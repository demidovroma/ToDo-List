package models.implicits

import anorm.{Column, Macro, RowParser, TypeDoesNotMatch}
import anorm.Macro.ColumnNaming
import models.db.Todo

trait ParserOps {

  implicit val columnToBoolean: Column[Boolean] =
    Column.nonNull { (value, meta) =>
      value match {
        case i: Int     => Right(i != 0)
        case b: Boolean => Right(b)
        case _ =>
          Left(TypeDoesNotMatch(
            s"Cannot convert $value:${value.asInstanceOf[AnyRef].getClass} to Boolean for column ${meta.column}"
          ))
      }
    }

  val todoParser: RowParser[Todo] =
    Macro.namedParser[Todo](ColumnNaming.SnakeCase)
}
