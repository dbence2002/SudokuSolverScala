package sudoku

import scala.annotation.{tailrec, targetName}
import scala.util.boundary
import scala.util.boundary.break

trait SolveState {
  def isSolved: Boolean
  def toSudokuTable: SudokuTable
}

trait SudokuSolver[State <: SolveState](using conv: Conversion[SudokuTable, State]) { self =>
  private type Solver = SudokuSolver[State]
  private type Table = SudokuTable

  def apply(st: State): Option[State]
  def apply(t: Table): Option[Table] = {
    if (t.isValid) this(conv.convert(t)).map(_.toSudokuTable) else None
  }
  def solve(t: Table): Option[Table] = {
    if (t.isValid) finish(conv.convert(t)).map(_.toSudokuTable) else None
  }
  def advance: Solver = this + this
  def advanceN(n: Int): Solver = new SudokuSolver[State] {
    override def apply(st: State): Option[State] = {
      @tailrec
      def advanceN_(n: Int, acc: State): Option[State] = n match {
        case 0 => Some(acc)
        case _ =>
          self(acc) match
            case None => None
            case Some(v) => advanceN_(n - 1, v)
      }
      advanceN_(n, st)
    }
  }
  def finish: Solver = new SudokuSolver[State] {
    override def apply(st: State): Option[State] = {
      @tailrec
      def finish_(acc: State): Option[State] = self(acc) match {
        case None => None
        case Some(v) if v.isSolved => Some(v)
        case Some(v) => finish_(v)
      }
      if (st.isSolved) Some(st) else finish_(st)
    }
  }
  @targetName("add")
  def +(s: Solver): Solver = new SudokuSolver[State] {
    override def apply(st: State): Option[State] = {
      val newSt = self(st)
      newSt match {
        case None => None
        case Some(v) => s(v)
      }
    }
  }
}

extension [A](t: Vector[Vector[A]])
  def updated2d(i: Int, j: Int, v: A): Vector[Vector[A]] = {
    t.updated(i, t(i).updated(j, v))
  }

object Augmenter {
  private val allNums = Set.range(1, 10)

  def augment(t: Vector[Vector[Int]]): Option[Vector[Vector[Int]]] = boundary:
    if (!SudokuTable(t).isValid) {
      return None
    }
    val rows: IndexedSeq[Set[Int]] = Range(0, 9).map(x => t(x).toSet)
    val cols: IndexedSeq[Set[Int]] = Range(0, 9).map(y => t.map(r => r(y)).toSet)
    val blocks: IndexedSeq[Set[Int]] = Range(0, 9).map(index => {
      val blockX = index / 3 * 3
      val blockY = index % 3 * 3
      t.slice(blockX, blockX + 3).flatMap(_.slice(blockY, blockY + 3)).toSet
    })
    val cacheFree: scala.collection.mutable.Map[(Int, Int), Set[Int]] = scala.collection.mutable.Map()
    def getFree(i: Int, j: Int): Set[Int] = {
      cacheFree.getOrElse((i, j), {
        val block = i / 3 * 3 + j / 3
        cacheFree.addOne((i, j), allNums.diff(rows(i)).diff(cols(j)).diff(blocks(block)))
        cacheFree((i, j))
      })
    }
    for {
      i <- 0 to 8
      j <- 0 to 8 if t(i)(j) == 0
    } {
      val block = i / 3 * 3 + j / 3
      val free = getFree(i, j)
      if (free.isEmpty) {
        break(None)
      }
      if (free.size == 1) {
        break(augment(t.updated2d(i, j, free.head)))
      }
    }
    val update = (i: Int, j: Int, v: Int) => {
      if (t(i)(j) != 0) {
        break(None)
      }
      break(augment(t.updated2d(i, j, v)))
    }
    Range(1, 10).foreach(v => {
      Range(0, 9).foreach(i => {
        val js = Range(0, 9).filter(j => getFree(i, j).contains(v))
        if (js.length == 1) {
          update(i, js.head, v)
        }
      })
      Range(0, 9).foreach(j => {
        val is = Range(0, 9).filter(i => getFree(i, j).contains(v))
        if (is.length == 1) {
          update(is.head, j, v)
        }
      })
      def transform(x: Int, y: Int): (Int, Int) = {
        val i = x / 3 * 3 + y / 3
        val j = x % 3 * 3 + y % 3
        (i, j)
      }
      Range(0, 9).foreach(x => {
        val ys = Range(0, 9).filter(y => {
          val (i, j) = transform(x, y)
          getFree(i, j).contains(v)
        })
        if (ys.length == 1) {
          val (i, j) = transform(x, ys.head)
          update(i, j, v)
        }
      })
    })
    Some(t)
}