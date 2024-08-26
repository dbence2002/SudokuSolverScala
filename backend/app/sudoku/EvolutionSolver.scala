package sudoku

import sudoku.conversion.given
import scala.collection.mutable.Set as MSet
import scala.collection.mutable.Map as MMap

val PopulationSize = 10
val ActionTournamentCount = 4
val CrossoverTournamentCount = 1
val MaxGenerationCount = 2500

private[sudoku] case class Cell(value: Int, locked: Boolean)
private[sudoku] class Partial(val table: Vector[Vector[Cell]]) {
  private val cacheUsed: MMap[(Int, Int), Set[Int]] = MMap()
  protected val rows: IndexedSeq[Set[Int]] = Range(0, 9).map(x => table(x).map(_.value).toSet)
  protected val cols: IndexedSeq[Set[Int]] = Range(0, 9).map(y => table.map(r => r(y).value).toSet)
  protected val blocks: IndexedSeq[Set[Int]] = Range(0, 9).map(index => {
    val blockX = index / 3 * 3
    val blockY = index % 3 * 3
    table.slice(blockX, blockX + 3).flatMap(_.slice(blockY, blockY + 3).map(_.value)).toSet
  })
  protected val settable: IndexedSeq[(Int, Int, Int)] = for {
    i <- 0 to 8
    j <- 0 to 8
    v <- 1 to 9 if table(i)(j).value == 0 && possibleToSet(i, j, v)
  } yield (i, j, v)
  protected val deletable: IndexedSeq[(Int, Int)] = for {
    i <- 0 to 8
    j <- 0 to 8 if table(i)(j).value != 0 && !table(i)(j).locked
  } yield (i, j)

  def getUsed(i: Int, j: Int): Set[Int] = {
    cacheUsed.getOrElse((i, j), {
      val block = i / 3 * 3 + j / 3
      cacheUsed.addOne((i, j), rows(i) ++ cols(j) ++ blocks(block))
      cacheUsed((i, j))
    })
  }
  def possibleToSet(i: Int, j: Int, v: Int): Boolean = {
    if (table(i)(j).value != 0 || v == 0) {
      return false;
    }
    !getUsed(i, j).contains(v)
  }
  def reset: Partial = {
    Partial(table.map(_.map({
      case Cell(v, l) if l => Cell(v, l)
      case Cell(v, l) => Cell(0, l)
    })))
  }
}
private[sudoku] case class EvolutionPartial(override val table: Vector[Vector[Cell]], fitness: (Int, Int)) extends Partial(table) {
  override def reset: EvolutionPartial = {
    EvolutionPartial(super.reset.table)
  }
  def crossover(other: EvolutionPartial, seed: Int): EvolutionPartial = {
    val r = scala.util.Random(seed)
    val rows = Vector.fill(9)(MSet[Int]())
    val cols = Vector.fill(9)(MSet[Int]())
    val blocks = Vector.fill(9)(MSet[Int]())
    val result = Array.fill(9)(Array.fill(9)(0))
    val indices = r.shuffle(for {
      i <- 0 to 8
      j <- 0 to 8
    } yield (i, j))

    def update(i: Int, j: Int, v: Int): Unit = {
      val block = i / 3 * 3 + j / 3
      val possible = rows(i) ++ cols(j) ++ blocks(block)
      if (possible.contains(v)) {
        return
      }
      result(i)(j) = v
      rows(i).add(v)
      cols(j).add(v)
      blocks(block).add(v)
    }
    for {
      i <- 0 to 8
      j <- 0 to 8
    } {
      if (table(i)(j).locked) {
        update(i, j, table(i)(j).value)
      }
    }
    indices.foreach((i, j) => {
      val val1 = table(i)(j).value
      val val2 = other.table(i)(j).value
      (val1, val2) match {
        case (0, 0) =>
        case (0, _) => update(i, j, val2)
        case (_, 0) => update(i, j, val1)
        case (_, _) => update(i, j, if (r.nextBoolean) val1 else val2)
      }
    })
    EvolutionPartial((for {
      i <- 0 to 8
      j <- 0 to 8
    } yield Cell(result(i)(j), table(i)(j).locked)).grouped(9).map(_.toVector).toVector)
  }
  def mutate(seed: Int): EvolutionPartial = {
    val r = scala.util.Random(seed)
    val actions = deletable.map(Left(_)) ++ settable.map(Right(_))
    val chosen = scala.util.Random(seed).shuffle(actions).slice(0, ActionTournamentCount)
    val best = chosen.maxBy({
      case Left(v) => (v._2, -1)
      case Right(v) => (v._2, 1)
    })
    best match {
      case Left((x, y)) =>
        val newTable = table.updated(x, table(x).updated(y, Cell(0, false)))
        EvolutionPartial(newTable)
      case Right((x, y, z)) =>
        val newTable = table.updated(x, table(x).updated(y, Cell(z, false)))
        EvolutionPartial(newTable)
    }
  }
}

private[sudoku] object EvolutionPartial {
  def apply(table: Vector[Vector[Cell]]): EvolutionPartial = {
    EvolutionPartial(table, fitness(table))
  }
}

private[sudoku] def fitness(table: Vector[Vector[Cell]]): (Int, Int) = {
  def sqSum(arr: Vector[Cell]): Int = {
    arr.count(_.value != 0)
  }
  def sumInRows(mat: Vector[Vector[Cell]], f: Vector[Cell] => Int): Int = {
    mat.map(row => f(row)).sum
  }
  val transpose = table.transpose
  var squareSum = 0
  squareSum += sumInRows(table, sqSum)
  squareSum += sumInRows(transpose, sqSum)
  squareSum += (for {
    startX <- 0 to 6 by 3
    startY <- 0 to 6 by 3
  } yield sqSum(table.slice(startX, startX + 3).flatMap(_.slice(startY, startY + 3)))).sum

  (table.map(row => row.count(_.value != 0)).sum, squareSum)
}

private case class EvolutionState(pop: Vector[EvolutionPartial], seed: Int, gen: Int) extends SolveState {
  override def isSolved: Boolean = {
    best.fitness._1 == 81
  }
  override def toSudokuTable: SudokuTable = {
    best.table.map(_.map(_.value))
  }
  def best: EvolutionPartial = {
    pop.maxBy(_.fitness)
  }
}

object EvolutionSolver extends SudokuSolver[EvolutionState] {
  override def apply(st: EvolutionState): Option[EvolutionState] = {
    println(st.best.fitness)
    if (st.isSolved) {
      return Some(st)
    }
    if (st.gen >= MaxGenerationCount) {
      return None
    }
    val r = scala.util.Random(st.seed)
    val sorted = st.pop.sortBy(_.fitness).reverse
    val offsprings = Range(0, PopulationSize).map(_ => {
      val s1 = sorted(Range(0, CrossoverTournamentCount).map(_ => r.between(0, st.pop.length)).min)
      val s2 = sorted(Range(0, CrossoverTournamentCount).map(_ => r.between(0, st.pop.length)).min)
      s1.crossover(s2, r.nextInt).mutate(r.nextInt).mutate(r.nextInt)
    }).toVector
    Some(EvolutionState(offsprings, r.nextInt, st.gen + 1))
  }
}

object EvolutionAugmenter extends SudokuSolver[EvolutionState] {
  override def apply(st: EvolutionState): Option[EvolutionState] = {
    if (st.isSolved) {
      return Some(st)
    }
    if (st.gen == MaxIterationCount) {
      return None
    }
    val newPop = st.pop.map(x => Augmenter.augment(x.table.map(_.map(_.value))) match {
      case None => x.reset
      case Some(t) =>
        EvolutionPartial((for {
          i <- 0 to 8
          j <- 0 to 8
        } yield Cell(t(i)(j), x.table(i)(j).locked)).grouped(9).map(_.toVector).toVector)
    })
    Some(st.copy(pop = newPop, seed = st.seed, gen = st.gen))
  }
}