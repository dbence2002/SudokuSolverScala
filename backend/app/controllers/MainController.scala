package controllers

import sudoku.{BacktrackSolver, EvolutionSolver, SudokuTable}
import sudoku.conversion.given

import javax.inject.*
import play.api.*
import play.api.libs.json.{JsSuccess, Json, Reads}
import play.api.libs.ws.WSClient
import play.api.mvc.*

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class SolveRequestData(algorithm: String, table: Vector[Vector[Int]])

@Singleton
class MainController @Inject()(ws: WSClient, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  implicit val decoder: Reads[SolveRequestData] = Json.reads[SolveRequestData]
  private val solvers = Map(
    "backtracking" -> BacktrackSolver,
    "evolutionary" -> EvolutionSolver
  )
  private val difficulties = Set("easy", "medium", "hard", "expert", "evil", "extreme")

  def solve(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    request.body.asJson.map(x => Json.fromJson[SolveRequestData](x)) match {
      case Some(JsSuccess(SolveRequestData(algo, table), _)) =>
        solvers(algo).solve(table) match {
          case None => Ok(Json.toJson(Map[String, String]("solution" -> null)))
          case Some(v) => Ok(Json.toJson(Map("solution" -> v.table)))
        }
      case _ => BadRequest("Invalid request")
    }
  }
  def importProblem(level: String): Action[AnyContent] = Action.async { _ =>
    if (!difficulties.contains(level)) {
      Future.successful(BadRequest("Invalid request"))
    } else {
      val request = ws.url(s"https://sudoku.com/api/v2/level/$level").addHttpHeaders("X-Requested-With" -> "XMLHttpRequest")
      request.get().map(resp => Try(resp.json).toOption match {
        case None => InternalServerError("Could not parse sudoku.com API response")
        case Some(json) => Ok(json)
      })
    }
  }
}
