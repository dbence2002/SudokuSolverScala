package sudoku

case class SudokuTable(table: Vector[Vector[Int]]) {
  override def toString: String = table.map(_.mkString(", ")).mkString("\n")

  def isSolved: Boolean = {
    isValid && table.map(_.count(_ != 0)).sum == 81
  }
  def isValid: Boolean = {
    val valid: Seq[Int] => Boolean = l => {
      val s = l.sorted
      s.zip(s.slice(1, s.length)).forall((x, y) => x == 0 || x != y) && s.head >= 0 && s.last <= 9
    }
    if (table.length != 9 || table.exists(r => r.length != 9)) {
      return false
    }
    if (!table.forall(x => valid(x))) {
      return false
    }
    if (!table.transpose.forall(x => valid(x))) {
      return false
    }
    val blocks = for {
      startX <- 0 to 6 by 3
      startY <- 0 to 6 by 3
    } yield valid(table.slice(startX, startX + 3).flatMap(_.slice(startY, startY + 3)))
    blocks.forall(x => x)
  }
}
