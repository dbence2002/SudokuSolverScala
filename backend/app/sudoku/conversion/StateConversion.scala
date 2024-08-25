package sudoku.conversion

import sudoku.{BacktrackState, Cell, Direction, EvolutionState, EvolutionPartial, PopulationSize, TabuPartial, SudokuTable, TabuSearchState}

import scala.collection.immutable.Queue

given Conversion[SudokuTable, BacktrackState] = (t: SudokuTable) =>
  BacktrackState(t.table, Nil, t.table.zipWithIndex.flatMap((r, i) => r.zipWithIndex.flatMap((x, j) =>
    if (x == 0) List((i, j)) else Nil
  )).toList, Direction.Forward)

given Conversion[SudokuTable, EvolutionState] = (t: SudokuTable) => {
  val table = t.table.map(_.map({
    case 0 => Cell(0, false)
    case x => Cell(x, true)
  }))
  EvolutionState(Range(0, PopulationSize).map(_ => EvolutionPartial(table)).toVector, 4, 0)
}

given Conversion[SudokuTable, TabuSearchState] = (t: SudokuTable) => {
  val table = t.table.map(_.map({
    case 0 => Cell(0, false)
    case x => Cell(x, true)
  }))
  TabuSearchState(Queue(TabuPartial(table)), Set(TabuPartial(table)), 0, 1)
}