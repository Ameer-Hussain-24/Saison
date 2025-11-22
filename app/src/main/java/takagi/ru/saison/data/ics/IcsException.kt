package takagi.ru.saison.data.ics

/**
 * ICS相关异常
 */
sealed class IcsException(message: String) : Exception(message) {
    class ParseError(message: String) : IcsException(message)
    class InvalidFormat(message: String) : IcsException(message)
    class IoError(message: String) : IcsException(message)
    class EmptyFile : IcsException("ICS文件为空或不包含课程数据")
}
