package sudoku

import sudoku.conversion.given

import scala.collection.immutable.Queue
import scala.util.{Try, boundary}

val MaxTabuListSize = 8
val MaxIterationCount = 25000
val TournamentSize = 3

private[sudoku] case class TabuPartial(override val table: Vector[Vector[Cell]], fitness: (Int, Int)) extends Partial(table) {
  override def reset: TabuPartial = {
    TabuPartial(super.reset.table)
  }
  private def tournamentChoose[A](it: IterableOnce[A], tabu: Set[TabuPartial])(f: A => TabuPartial): Option[TabuPartial] = {
    Try(
      LazyList.from(it).map(f).filter(neigh => !tabu.contains(neigh)).take(TournamentSize).maxBy(_.fitness)
    ).toOption
  }
  def bestNeighbour(tabu: Set[TabuPartial], seed: Int): TabuPartial = boundary:
    val r = scala.util.Random(seed)
    val chooseSet = tournamentChoose(r.shuffle(settable), tabu)((i, j, v) =>
      TabuPartial(table.updated2d(i, j, Cell(v, false)))
    )
    chooseSet match {
      case Some(v) => return v
      case None =>
        val chooseDel = tournamentChoose(r.shuffle(deletable), tabu)((i, j) =>
          TabuPartial(table.updated2d(i, j, Cell(0, false)))
        )
        chooseDel match {
          case Some(v) => return v
          case None => this
        }
    }
  }

private[sudoku] object TabuPartial {
  def apply(table: Vector[Vector[Cell]]): TabuPartial = {
    TabuPartial(table, fitness(table))
  }
}

private case class TabuSearchState(q: Queue[TabuPartial], vis: Set[TabuPartial], seed: Int, it: Int) extends SolveState {
  override def isSolved: Boolean = {
    best.fitness._1 == 81
  }
  override def toSudokuTable: SudokuTable = {
    best.table.map(_.map(_.value))
  }
  def best: TabuPartial = {
    q.maxBy(_.fitness)
  }
}

object TabuSearchSolver extends SudokuSolver[TabuSearchState] {
  override def apply(st: TabuSearchState): Option[TabuSearchState] = {
    if (st.isSolved) {
      return Some(st)
    }
    if (st.it == MaxIterationCount) {
      return None
    }
    val r = scala.util.Random(st.seed)
    val bestN = st.best.bestNeighbour(st.vis, r.nextInt)
    val addQ = st.q.enqueue(bestN)
    if (st.q.size < MaxTabuListSize) {
      return Some(st.copy(q = addQ, vis = st.vis + bestN, seed = r.nextInt, it = st.it + 1))
    }
    val (fr, delQ) = addQ.dequeue
    Some(st.copy(q = delQ, vis = st.vis + bestN - fr, seed = r.nextInt, it = st.it + 1))
  }
}

object TabuSearchAugmenter extends SudokuSolver[TabuSearchState] {
  override def apply(st: TabuSearchState): Option[TabuSearchState] = {
    if (st.isSolved) {
      return Some(st)
    }
    if (st.it == MaxIterationCount) {
      return None
    }
    val fr = st.q.last
    val augFr = Augmenter.augment(fr.table.map(_.map(_.value))) match {
      case None => fr.reset
      case Some(t) =>
        TabuPartial((for {
          i <- 0 to 8
          j <- 0 to 8
        } yield Cell(t(i)(j), fr.table(i)(j).locked)).grouped(9).map(_.toVector).toVector)
    }
    val newQ = st.q.dropRight(1).enqueue(augFr)
    Some(st.copy(q = newQ, vis = newQ.toSet, seed = st.seed, it = st.it))
  }
}