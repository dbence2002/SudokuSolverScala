package sudoku

import sudoku.conversion.given

val PopulationSize = 18
val MutateCount = 18
val ActionTournamentCount = 5
val MutateTournamentCount = 2
val MaxGenerationCount = 25000

private[sudoku] case class Cell(value: Int, locked: Boolean)
private[sudoku] case class Individual(table: Vector[Vector[Cell]], fitness: (Int, Int)) {
  private def collisionCount(i: Int, j: Int, v: Int): Int = {
    val cntRow = table(i).count(_.value == v)
    val cntCol = table.count(_(j).value == v)

    val startX = i / 3 * 3
    val startY = j / 3 * 3
    val cntBlock = table.slice(startX, startX + 3).flatMap(_.slice(startY, startY + 3)).count(_.value == v)

    val corr = if (table(i)(j).value == v) 3 else 0
    cntRow + cntCol + cntBlock - corr
  }
  private def scoreOfDel(i: Int, j: Int): (Int, Int) = table(i)(j) match {
    case Cell(0, _) => (0, 0)
    case Cell(_, true) => (0, 0)
    case _ =>
      (collisionCount(i, j, table(i)(j).value) - 1, 0)
  }
  private def scoreOfSet(i: Int, j: Int, v: Int): (Int, Int) = {
    (1 - collisionCount(i, j, v), 0)
  }
  private def possibleSet: Vector[(Int, Int, Int)] = {
    val settable = for {
      i <- 0 to 8
      j <- 0 to 8 if !table(i)(j).locked && table(i)(j).value == 0
      v <- 1 to 9
    } yield (i, j, v)
    settable.toVector
  }
  private def possibleDel: Vector[(Int, Int)] = {
    val deletable = for {
      i <- 0 to 8
      j <- 0 to 8 if !table(i)(j).locked && table(i)(j).value != 0
    } yield (i, j)
    deletable.toVector
  }
  def mutate(seed: Int): Individual = {
    val actions = possibleDel.map(Left(_)) ++ possibleSet.map(Right(_))
    val chosen = scala.util.Random(seed).shuffle(actions).slice(0, ActionTournamentCount).map({
      case Left((i, j)) => Left((i, j), scoreOfDel(i, j))
      case Right((i, j, v)) => Right((i, j, v), scoreOfSet(i, j, v))
    })
    val best = chosen.maxBy({
      case Left(v) => v._2
      case Right(v) => v._2
    })
    best match {
      case Left(v) =>
        val (x, y) = v._1
        val newTable = table.updated(x, table(x).updated(y, Cell(0, false)))
        Individual(newTable)
      case Right(v) =>
        val (x, y, z) = v._1
        val newTable = table.updated(x, table(x).updated(y, Cell(z, false)))
        Individual(newTable)
    }
  }
}

private[sudoku] object Individual {
  def apply(table: Vector[Vector[Cell]]): Individual = {
    Individual(table, fitness(table))
  }
}

private[sudoku] def fitness(table: Vector[Vector[Cell]]): (Int, Int) = {
  val cntNot0 = table.map(row => row.count(_.value != 0)).sum
  var collision = 0

  def collCnt(arr: Vector[Cell]): Int = {
    val s = arr.map(_.value).sorted
    s.zip(s.slice(1, s.length)).count((x, y) => x != 0 && x == y)
  }
  def sqSum(arr: Vector[Cell]): Int = {
    arr.count(_.value != 0)
  }
  def sumInRows(mat: Vector[Vector[Cell]], f: Vector[Cell] => Int): Int = {
    mat.map(row => f(row)).sum
  }
  val transpose = table.transpose
  collision += sumInRows(table, collCnt)
  collision += sumInRows(transpose, collCnt)
  collision += (for {
    startX <- 0 to 6 by 3
    startY <- 0 to 6 by 3
  } yield collCnt(table.slice(startX, startX + 3).flatMap(_.slice(startY, startY + 3)))).sum

  var squareSum = 0
  squareSum += sumInRows(table, sqSum)
  squareSum += sumInRows(transpose, sqSum)
  squareSum += (for {
    startX <- 0 to 6 by 3
    startY <- 0 to 6 by 3
  } yield sqSum(table.slice(startX, startX + 3).flatMap(_.slice(startY, startY + 3)))).sum
  (cntNot0 - collision, squareSum)
}

private case class EvolutionState(pop: Vector[Individual], seed: Int, gen: Int) extends SolveState {
  override def isSolved: Boolean = {
    pop.exists(_.fitness._1 == 81)
  }
  override def toSudokuTable: SudokuTable = {
    val best = pop.maxBy(_.fitness)
    best.table.map(_.map(_.value))
  }
}

object EvolutionSolver extends SudokuSolver[EvolutionState] {
  override def apply(st: EvolutionState): Option[EvolutionState] = {
    if (st.isSolved) {
      return Some(st)
    }
    if (st.gen >= MaxGenerationCount) {
      return None
    }
    val r = scala.util.Random(st.seed)
    val sorted = st.pop.sortBy(_.fitness).reverse
    val mutated = Range(0, MutateCount).map(_ => {
      val index = Range(0, MutateTournamentCount).map(_ => r.between(0, st.pop.length)).min
      sorted(index).mutate(r.nextInt)
    }).toVector
    Some(EvolutionState(mutated, r.nextInt, st.gen + 1))
  }
}
