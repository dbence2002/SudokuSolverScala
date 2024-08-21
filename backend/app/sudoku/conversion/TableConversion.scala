package sudoku.conversion

import sudoku.SudokuTable

given Conversion[Vector[Vector[Int]], SudokuTable] = v => SudokuTable(v)
given Conversion[SudokuTable, Vector[Vector[Int]]] = t => t.table
