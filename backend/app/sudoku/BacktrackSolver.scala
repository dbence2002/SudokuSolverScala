package sudoku

import sudoku.conversion.given

import scala.util.Try

private enum Direction {
  case Forward
  case Backward
}

private case class BacktrackState(
  table: Vector[Vector[Int]],
  used: List[(Int, Int)],
  free: List[(Int, Int)],
  direction: Direction) extends SolveState {

  override def isSolved: Boolean = {
    free.isEmpty
  }
  override def toSudokuTable: SudokuTable = {
    SudokuTable(table)
  }
  def availableNums(x: Int, y: Int): Set[Int] = {
    val row = table(x).toSet
    val col = table.map(r => r(y)).toSet
    val blockX = x / 3 * 3
    val blockY = y / 3 * 3
    val block = table.slice(blockX, blockX + 3).flatMap(_.slice(blockY, blockY + 3))
    Range(1, 10).toSet.diff(row ++ col ++ block)
  }
  def append(v: Int): BacktrackState = {
    val (x, y) = free.head
    copy(table = table.updated2d(x, y, v), used = (x, y) :: used, free = free.tail, direction = Direction.Forward)
  }
  def undo: BacktrackState = {
    val (x, y) = used.head
    copy(table = table.updated2d(x, y, 0), used = used.tail, free = (x, y) :: free, direction = Direction.Backward)
  }
  def changeHead(v: Int): BacktrackState = {
    val (x, y) = used.head
    copy(table = table.updated2d(x, y, v), used = used, free = free, direction = Direction.Forward)
  }
}

object BacktrackSolver extends SudokuSolver[BacktrackState] {
  private type State = BacktrackState
  private type Table = SudokuTable
  
  private def advanceHead(st: State): Option[Int] = {
    val (x, y) = st.used.head
    Try(st.undo.availableNums(x, y).filter(_ > st.table(x)(y)).min).toOption
  }
  override def apply(st: State): Option[State] = st.free match {
    case Nil => Some(st)
    case h :: t =>
      val (x, y) = h
      val free = st.availableNums(x, y)
      if (free.isEmpty || st.direction == Direction.Backward) {
        if (st.used.isEmpty) {
          return None
        }
        advanceHead(st) match {
          case None => Some(st.undo)
          case Some(v) => Some(st.changeHead(v))
        }
      } else {
        Some(st.append(free.min))
      }
  }
}