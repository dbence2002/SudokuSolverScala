package sudoku

import scala.annotation.{tailrec, targetName}

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