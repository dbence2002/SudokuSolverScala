import sudoku.{BacktrackSolver, BacktrackState, EvolutionSolver, EvolutionState, SudokuSolver, SudokuTable}
import sudoku.conversion.given
import zio.*
import zio.http.*
import zio.http.Header.{AccessControlAllowOrigin, Origin}
import zio.http.Middleware.{CorsConfig, cors}
import zio.json.*
import zio.json.internal.Write

case class SolveRequestData(table: Vector[Vector[Int]], algorithm: String)
case class SolveResponseData(solution: Option[SudokuTable])

given [A](using encoder: JsonEncoder[A]): JsonEncoder[Option[A]] =
  (opt: Option[A], indent: Option[Int], out: Write) => {
    opt match {
      case Some(value) => encoder.unsafeEncode(value, indent, out)
      case None => out.write("null")
    }
  }

object Main extends ZIOAppDefault {
  given JsonEncoder[SudokuTable] = DeriveJsonEncoder.gen[SudokuTable]
  given JsonDecoder[SolveRequestData] = DeriveJsonDecoder.gen[SolveRequestData]
  given JsonEncoder[SolveResponseData] = DeriveJsonEncoder.gen[SolveResponseData]
  
  private val solvers = Map(
    "evolutionary" -> EvolutionSolver,
    "backtracking" -> BacktrackSolver
  )
  private val config: CorsConfig =
    CorsConfig(
      allowedOrigin = {
        case origin if origin == Origin.parse(sys.env("FRONTEND_URL")).toOption.get =>
          Some(AccessControlAllowOrigin.Specific(origin))
        case _ => None
      },
    )
  private val routes =
    Routes(
      Method.GET / Root -> handler(Response.text("Greetings at your service")),
      Method.POST / "solve" -> handler { (req: Request) =>
        (for {
          parsed <- req.body.asString.map(_.fromJson[SolveRequestData])
          resp <- parsed match {
            case Right(r) if solvers.contains(r.algorithm) =>
              solvers(r.algorithm).solve(r.table) match {
                case Some(v) if v.isSolved =>
                  ZIO.attempt(Response.json(SolveResponseData(Some(v)).toJson))
                case _ =>
                  ZIO.attempt(Response.json(SolveResponseData(None).toJson))
              }
            case _ => ZIO.attempt(Response.badRequest("Invalid request"))
          }
        } yield resp).orDie
      }
    ) @@ cors(config)

  def run: ZIO[Any, Throwable, Nothing] = Server
    .serve(routes)
    .provide(Server.defaultWithPort(8080))
}
