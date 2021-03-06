/**
  * Created by Dan on 5/4/2017.
  */
trait CSVFormat {

  val delimiter: Char

  val quoteChar: Char

  val escapeChar: Char

  val lineTerminator: String

  val quoting: Quoting

  val treatEmptyLineAsNil: Boolean

}
