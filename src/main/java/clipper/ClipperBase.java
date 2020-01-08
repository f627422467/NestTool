package clipper;

import clipper.Path.OutRec;
import clipper.Point.LongPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class ClipperBase implements Clipper {
    protected class LocalMinima {
        long y;
        Edge leftBound;
        Edge rightBound;
        LocalMinima next;
    }

    protected class Scanbeam {
        long y;
        Scanbeam next;
    }

    protected class Maxima {
        long x;
        Maxima next;
        Maxima prev;
    }

    private static void initEdge(Edge e, Edge eNext, Edge ePrev, LongPoint pt ) {
        e.next = eNext;
        e.prev = ePrev;
        e.setCurrent( new LongPoint( pt ) );
        e.outIdx = Edge.UNASSIGNED;
    }

    private static void initEdge2(Edge e, PolyType polyType ) {
        if (e.getCurrent().getY() >= e.next.getCurrent().getY()) {
            e.setBot( new LongPoint( e.getCurrent() ) );
            e.setTop( new LongPoint( e.next.getCurrent() ) );
        }
        else {
            e.setTop( new LongPoint( e.getCurrent() ) );
            e.setBot( new LongPoint( e.next.getCurrent() ) );
        }
        e.updateDeltaX();
        e.polyTyp = polyType;
    }

    private static void rangeTest( LongPoint Pt ) {

        if (Pt.getX() > LOW_RANGE || Pt.getY() > LOW_RANGE || -Pt.getX() > LOW_RANGE || -Pt.getY() > LOW_RANGE) {
            if (Pt.getX() > HI_RANGE || Pt.getY() > HI_RANGE || -Pt.getX() > HI_RANGE || -Pt.getY() > HI_RANGE) {
                throw new IllegalStateException( "Coordinate outside allowed range" );
            }
        }
    }

    private static Edge removeEdge(Edge e ) {
        //removes e from double_linked_list (but without removing from memory)
        e.prev.next = e.next;
        e.next.prev = e.prev;
        final Edge result = e.next;
        e.prev = null; //flag as removed (see ClipperBase.Clear)
        return result;
    }

    private final static long LOW_RANGE = 0x3FFFFFFF;

    private final static long HI_RANGE = 0x3FFFFFFFFFFFFFFFL;

    protected LocalMinima minimaList;

    protected LocalMinima currentLM;

    protected Scanbeam scanbeam;

    protected final List<OutRec> polyOuts = new ArrayList<>();

    protected Edge activeEdges;

    protected boolean hasOpenPaths;

    protected final boolean preserveCollinear;

    private final static Logger LOGGER = Logger.getLogger( Clipper.class.getName() );

    protected ClipperBase( boolean preserveCollinear ) //constructor (nb: no external instantiation)
    {
        this.preserveCollinear = preserveCollinear;
        minimaList = null;
        currentLM = null;
        hasOpenPaths = false;
    }

    @Override
    public boolean addPath(Path pg, PolyType polyType, boolean Closed ) {

        if (!Closed && polyType == PolyType.CLIP) {
            throw new IllegalStateException( "AddPath: Open paths must be subject." );
        }

        int highI = pg.size() - 1;
        if (Closed) {
            while (highI > 0 && pg.get( highI ).equals( pg.get( 0 ) )) {
                --highI;
            }
        }
        while (highI > 0 && pg.get( highI ).equals( pg.get( highI - 1 ) )) {
            --highI;
        }
        if (Closed && highI < 2 || !Closed && highI < 1) {
            return false;
        }

        //create a new edge array ...
        final List<Edge> edges = new ArrayList<>( highI + 1 );
        for (int i = 0; i <= highI; i++) {
            edges.add( new Edge() );
        }

        boolean IsFlat = true;

        //1. Basic (first) edge initialization ...
        edges.get( 1 ).setCurrent( new LongPoint( pg.get( 1 ) ) );
        rangeTest( pg.get( 0 ) );
        rangeTest( pg.get( highI ) );
        initEdge( edges.get( 0 ), edges.get( 1 ), edges.get( highI ), pg.get( 0 ) );
        initEdge( edges.get( highI ), edges.get( 0 ), edges.get( highI - 1 ), pg.get( highI ) );
        for (int i = highI - 1; i >= 1; --i) {
            rangeTest( pg.get( i ) );
            initEdge( edges.get( i ), edges.get( i + 1 ), edges.get( i - 1 ), pg.get( i ) );
        }
        Edge eStart = edges.get( 0 );

        //2. Remove duplicate vertices, and (when closed) collinear edges ...
        Edge e = eStart, eLoopStop = eStart;
        for (;;) {
            //nb: allows matching start and end points when not Closed ...
            if (e.getCurrent().equals( e.next.getCurrent() ) && (Closed || !e.next.equals( eStart ))) {
                if (e == e.next) {
                    break;
                }
                if (e == eStart) {
                    eStart = e.next;
                }
                e = removeEdge( e );
                eLoopStop = e;
                continue;
            }
            if (e.prev == e.next) {
                break; //only two vertices
            }
            else if (Closed && Point.slopesEqual( e.prev.getCurrent(), e.getCurrent(), e.next.getCurrent() )
                            && (!isPreserveCollinear() || !Point.isPt2BetweenPt1AndPt3( e.prev.getCurrent(), e.getCurrent(), e.next.getCurrent() ))) {
                //Collinear edges are allowed for open paths but in closed paths
                //the default is to merge adjacent collinear edges into a single edge.
                //However, if the PreserveCollinear property is enabled, only overlapping
                //collinear edges (ie spikes) will be removed from closed paths.
                if (e == eStart) {
                    eStart = e.next;
                }
                e = removeEdge( e );
                e = e.prev;
                eLoopStop = e;
                continue;
            }
            e = e.next;
            if (e == eLoopStop || !Closed && e.next == eStart) {
                break;
            }
        }

        if (!Closed && e == e.next || Closed && e.prev == e.next) {
            return false;
        }

        if (!Closed) {
            hasOpenPaths = true;
            eStart.prev.outIdx = Edge.SKIP;
        }

        //3. Do second stage of edge initialization ...
        e = eStart;
        do {
            initEdge2( e, polyType );
            e = e.next;
            if (IsFlat && e.getCurrent().getY() != eStart.getCurrent().getY()) {
                IsFlat = false;
            }
        }
        while (e != eStart);

        //4. Finally, add edge bounds to LocalMinima list ...

        //Totally flat paths must be handled differently when adding them
        //to LocalMinima list to avoid endless loops etc ...
        if (IsFlat) {
            if (Closed) {
                return false;
            }
            e.prev.outIdx = Edge.SKIP;
            final LocalMinima locMin = new LocalMinima();
            locMin.next = null;
            locMin.y = e.getBot().getY();
            locMin.leftBound = null;
            locMin.rightBound = e;
            locMin.rightBound.side = Edge.Side.RIGHT;
            locMin.rightBound.windDelta = 0;
            for ( ; ; ) {
                if (e.getBot().getX() != e.prev.getTop().getX()) {
                    e.reverseHorizontal();
                }
                if (e.next.outIdx == Edge.SKIP) break;
                e.nextInLML = e.next;
                e = e.next;
            }
            insertLocalMinima( locMin );
            return true;
        }

        boolean leftBoundIsForward;
        Edge EMin = null;

        //workaround to avoid an endless loop in the while loop below when
        //open paths have matching start and end points ...
        if (e.prev.getBot().equals( e.prev.getTop() )) {
            e = e.next;
        }

        for (;;) {
            e = e.findNextLocMin();
            if (e == EMin) {
                break;
            }
            else if (EMin == null) {
                EMin = e;
            }

            //E and E.Prev now share a local minima (left aligned if horizontal).
            //Compare their slopes to find which starts which bound ...
            final LocalMinima locMin = new LocalMinima();
            locMin.next = null;
            locMin.y = e.getBot().getY();
            if (e.deltaX < e.prev.deltaX) {
                locMin.leftBound = e.prev;
                locMin.rightBound = e;
                leftBoundIsForward = false; //Q.nextInLML = Q.prev
            }
            else {
                locMin.leftBound = e;
                locMin.rightBound = e.prev;
                leftBoundIsForward = true; //Q.nextInLML = Q.next
            }
            locMin.leftBound.side = Edge.Side.LEFT;
            locMin.rightBound.side = Edge.Side.RIGHT;

            if (!Closed) {
                locMin.leftBound.windDelta = 0;
            }
            else if (locMin.leftBound.next == locMin.rightBound) {
                locMin.leftBound.windDelta = -1;
            }
            else {
                locMin.leftBound.windDelta = 1;
            }
            locMin.rightBound.windDelta = -locMin.leftBound.windDelta;

            e = processBound( locMin.leftBound, leftBoundIsForward );
            if (e.outIdx == Edge.SKIP) {
                e = processBound( e, leftBoundIsForward );
            }

            Edge E2 = processBound( locMin.rightBound, !leftBoundIsForward );
            if (E2.outIdx == Edge.SKIP) {
                E2 = processBound( E2, !leftBoundIsForward );
            }

            if (locMin.leftBound.outIdx == Edge.SKIP) {
                locMin.leftBound = null;
            }
            else if (locMin.rightBound.outIdx == Edge.SKIP) {
                locMin.rightBound = null;
            }
            insertLocalMinima( locMin );
            if (!leftBoundIsForward) {
                e = E2;
            }
        }
        return true;

    }

    @Override
    public boolean addPaths(Paths paths, PolyType polyType, boolean closed ) {
        boolean result = false;
        for (Path path : paths) {
            if (addPath(path, polyType, closed)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public void clear() {
        disposeLocalMinimaList();
        hasOpenPaths = false;
    }

    private void disposeLocalMinimaList() {
        while (minimaList != null) {
            final LocalMinima tmpLm = minimaList.next;
            minimaList = null;
            minimaList = tmpLm;
        }
        currentLM = null;
    }

    private void insertLocalMinima( LocalMinima newLm ) {
        if (minimaList == null) {
            minimaList = newLm;
        }
        else if (newLm.y >= minimaList.y) {
            newLm.next = minimaList;
            minimaList = newLm;
        }
        else {
            LocalMinima tmpLm = minimaList;
            while (tmpLm.next != null && newLm.y < tmpLm.next.y) {
                tmpLm = tmpLm.next;
            }
            newLm.next = tmpLm.next;
            tmpLm.next = newLm;
        }
    }
    private boolean isPreserveCollinear() {
        return preserveCollinear;
    }

    protected boolean popLocalMinima( long y, LocalMinima[] current ) {
        LOGGER.entering( ClipperBase.class.getName(), "popLocalMinima" );
        current[0] = currentLM;
        if (currentLM != null && currentLM.y == y) {
            currentLM = currentLM.next;
            return true;
        }
        return false;
    }

    private Edge processBound(Edge e, boolean LeftBoundIsForward ) {
        Edge EStart, result = e;
        Edge Horz;

        if (result.outIdx == Edge.SKIP) {
            //check if there are edges beyond the skip edge in the bound and if so
            //create another LocMin and calling ProcessBound once more ...
            e = result;
            if (LeftBoundIsForward) {
                while (e.getTop().getY() == e.next.getBot().getY()) {
                    e = e.next;
                }
                while (e != result && e.deltaX == Edge.HORIZONTAL) {
                    e = e.prev;
                }
            }
            else {
                while (e.getTop().getY() == e.prev.getBot().getY()) {
                    e = e.prev;
                }
                while (e != result && e.deltaX == Edge.HORIZONTAL) {
                    e = e.next;
                }
            }
            if (e == result) {
                if (LeftBoundIsForward) {
                    result = e.next;
                }
                else {
                    result = e.prev;
                }
            }
            else {
                //there are more edges in the bound beyond result starting with E
                if (LeftBoundIsForward) {
                    e = result.next;
                }
                else {
                    e = result.prev;
                }
                final LocalMinima locMin = new LocalMinima();
                locMin.next = null;
                locMin.y = e.getBot().getY();
                locMin.leftBound = null;
                locMin.rightBound = e;
                e.windDelta = 0;
                result = processBound( e, LeftBoundIsForward );
                insertLocalMinima( locMin );
            }
            return result;
        }

        if (e.deltaX == Edge.HORIZONTAL) {
            //We need to be careful with open paths because this may not be a
            //true local minima (ie E may be following a skip edge).
            //Also, consecutive horz. edges may start heading left before going right.
            if (LeftBoundIsForward) {
                EStart = e.prev;
            }
            else {
                EStart = e.next;
            }
            if (EStart.deltaX == Edge.HORIZONTAL) //ie an adjoining horizontal skip edge
            {
                if (EStart.getBot().getX() != e.getBot().getX() && EStart.getTop().getX() != e.getBot().getX()) {
                    e.reverseHorizontal();
                }
            }
            else if (EStart.getBot().getX() != e.getBot().getX()) {
                e.reverseHorizontal();
            }
        }

        EStart = e;
        if (LeftBoundIsForward) {
            while (result.getTop().getY() == result.next.getBot().getY() && result.next.outIdx != Edge.SKIP) {
                result = result.next;
            }
            if (result.deltaX == Edge.HORIZONTAL && result.next.outIdx != Edge.SKIP) {
                //nb: at the top of a bound, horizontals are added to the bound
                //only when the preceding edge attaches to the horizontal's left vertex
                //unless a Skip edge is encountered when that becomes the top divide
                Horz = result;
                while (Horz.prev.deltaX == Edge.HORIZONTAL) {
                    Horz = Horz.prev;
                }
                if (Horz.prev.getTop().getX() > result.next.getTop().getX()) {
                    result = Horz.prev;
                }
            }
            while (e != result) {
                e.nextInLML = e.next;
                if (e.deltaX == Edge.HORIZONTAL && e != EStart && e.getBot().getX() != e.prev.getTop().getX()) {
                    e.reverseHorizontal();
                }
                e = e.next;
            }
            if (e.deltaX == Edge.HORIZONTAL && e != EStart && e.getBot().getX() != e.prev.getTop().getX()) {
                e.reverseHorizontal();
            }
            result = result.next; //move to the edge just beyond current bound
        }
        else {
            while (result.getTop().getY() == result.prev.getBot().getY() && result.prev.outIdx != Edge.SKIP) {
                result = result.prev;
            }
            if (result.deltaX == Edge.HORIZONTAL && result.prev.outIdx != Edge.SKIP) {
                Horz = result;
                while (Horz.next.deltaX == Edge.HORIZONTAL) {
                    Horz = Horz.next;
                }
                if (Horz.next.getTop().getX() == result.prev.getTop().getX() ||
                    Horz.next.getTop().getX() > result.prev.getTop().getX()) {
                        result = Horz.next;
                }
            }

            while (e != result) {
                e.nextInLML = e.prev;
                if (e.deltaX == Edge.HORIZONTAL && e != EStart && e.getBot().getX() != e.next.getTop().getX()) {
                    e.reverseHorizontal();
                }
                e = e.prev;
            }
            if (e.deltaX == Edge.HORIZONTAL && e != EStart && e.getBot().getX() != e.next.getTop().getX()) {
                e.reverseHorizontal();
            }
            result = result.prev; //move to the edge just beyond current bound
        }
        return result;
    }

    protected void reset() {
        currentLM = minimaList;
        if (currentLM == null) {
            return; //ie nothing to process
        }

        //reset all edges ...
        scanbeam = null;
        LocalMinima lm = minimaList;
        while (lm != null) {
            insertScanbeam(lm.y);
            Edge e = lm.leftBound;
            if (e != null) {
                e.setCurrent( new LongPoint( e.getBot() ) );
                e.outIdx = Edge.UNASSIGNED;
            }
            e = lm.rightBound;
            if (e != null) {
                e.setCurrent( new LongPoint( e.getBot() ) );
                e.outIdx = Edge.UNASSIGNED;
            }
            lm = lm.next;
        }
        activeEdges = null;
    }

    protected void insertScanbeam( long y ) {
        LOGGER.entering( ClipperBase.class.getName(), "insertScanbeam" );

        //single-linked list: sorted descending, ignoring dups.
        if (scanbeam == null) {
            scanbeam = new Scanbeam();
            scanbeam.next = null;
            scanbeam.y = y;
        }
        else if (y > scanbeam.y) {
            final Scanbeam newSb = new Scanbeam();
            newSb.y = y;
            newSb.next = scanbeam;
            scanbeam = newSb;
        }
        else {
            Scanbeam sb2 = scanbeam;
            while (sb2.next != null && (y <= sb2.next.y)) {
                sb2 = sb2.next;
            }
            if (y == sb2.y) {
                return; //ie ignores duplicates
            }
            final Scanbeam newSb = new Scanbeam();
            newSb.y = y;
            newSb.next = sb2.next;
            sb2.next = newSb;
        }
    }

    protected boolean popScanbeam( long[] y ) {
        if (scanbeam == null) {
            y[0] = 0;
            return false;
        }
        y[0] = scanbeam.y;
        scanbeam = scanbeam.next;
        return true;
    }

    protected final boolean localMinimaPending() {
        return currentLM != null;
    }

    protected OutRec createOutRec() {
        OutRec result = new OutRec();
        result.Idx = Edge.UNASSIGNED;
        result.isHole = false;
        result.isOpen = false;
        result.firstLeft = null;
        result.setPoints( null );
        result.bottomPt = null;
        result.polyNode = null;
        polyOuts.add( result );
        result.Idx = polyOuts.size() - 1;
        return result;
    }

    protected void disposeOutRec( int index ) {
        OutRec outRec = polyOuts.get( index );
        outRec.setPoints( null );
        outRec = null;
        polyOuts.set( index, null );
    }

    protected void updateEdgeIntoAEL( Edge e ) {
        if (e.nextInLML == null) {
            throw new IllegalStateException("UpdateEdgeIntoAEL: invalid call");
        }
        final Edge aelPrev = e.prevInAEL;
        final Edge aelNext = e.nextInAEL;
        e.nextInLML.outIdx = e.outIdx;
        if (aelPrev != null) {
            aelPrev.nextInAEL = e.nextInLML;
        }
        else {
            activeEdges = e.nextInLML;
        }
        if (aelNext != null) {
            aelNext.prevInAEL = e.nextInLML;
        }
        e.nextInLML.side = e.side;
        e.nextInLML.windDelta = e.windDelta;
        e.nextInLML.windCnt = e.windCnt;
        e.nextInLML.windCnt2 = e.windCnt2;
        e = e.nextInLML;
        e.setCurrent(e.getBot());
        e.prevInAEL = aelPrev;
        e.nextInAEL = aelNext;
        if (e.isHorizontal()) {
            insertScanbeam(e.getTop().getY());
        }
    }

    protected void swapPositionsInAEL(Edge edge1, Edge edge2 ) {
        LOGGER.entering( ClipperBase.class.getName(), "swapPositionsInAEL" );

        //check that one or other edge hasn't already been removed from AEL ...
        if (edge1.nextInAEL == edge1.prevInAEL || edge2.nextInAEL == edge2.prevInAEL) {
            return;
        }

        if (edge1.nextInAEL == edge2) {
            final Edge next = edge2.nextInAEL;
            if (next != null) {
                next.prevInAEL = edge1;
            }
            final Edge prev = edge1.prevInAEL;
            if (prev != null) {
                prev.nextInAEL = edge2;
            }
            edge2.prevInAEL = prev;
            edge2.nextInAEL = edge1;
            edge1.prevInAEL = edge2;
            edge1.nextInAEL = next;
        }
        else if (edge2.nextInAEL == edge1) {
            final Edge next = edge1.nextInAEL;
            if (next != null) {
                next.prevInAEL = edge2;
            }
            final Edge prev = edge2.prevInAEL;
            if (prev != null) {
                prev.nextInAEL = edge1;
            }
            edge1.prevInAEL = prev;
            edge1.nextInAEL = edge2;
            edge2.prevInAEL = edge1;
            edge2.nextInAEL = next;
        }
        else {
            final Edge next = edge1.nextInAEL;
            final Edge prev = edge1.prevInAEL;
            edge1.nextInAEL = edge2.nextInAEL;
            if (edge1.nextInAEL != null) {
                edge1.nextInAEL.prevInAEL = edge1;
            }
            edge1.prevInAEL = edge2.prevInAEL;
            if (edge1.prevInAEL != null) {
                edge1.prevInAEL.nextInAEL = edge1;
            }
            edge2.nextInAEL = next;
            if (edge2.nextInAEL != null) {
                edge2.nextInAEL.prevInAEL = edge2;
            }
            edge2.prevInAEL = prev;
            if (edge2.prevInAEL != null) {
                edge2.prevInAEL.nextInAEL = edge2;
            }
        }

        if (edge1.prevInAEL == null) {
            activeEdges = edge1;
        }
        else if (edge2.prevInAEL == null) {
            activeEdges = edge2;
        }

        LOGGER.exiting( ClipperBase.class.getName(), "swapPositionsInAEL" );
    }

    protected void deleteFromAEL( Edge e ) {
        LOGGER.entering( ClipperBase.class.getName(), "deleteFromAEL" );

        Edge aelPrev = e.prevInAEL;
        Edge aelNext = e.nextInAEL;
        if (aelPrev == null && aelNext == null && (e != activeEdges)) {
            return; //already deleted
        }
        if (aelPrev != null) {
            aelPrev.nextInAEL = aelNext;
        }
        else {
        	activeEdges = aelNext;
        }
        if (aelNext != null) {
            aelNext.prevInAEL = aelPrev;
        }
        e.nextInAEL = null;
        e.prevInAEL = null;

        LOGGER.exiting( ClipperBase.class.getName(), "deleteFromAEL" );
    }
}