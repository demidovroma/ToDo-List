package utils

object Messages {
  object Todo {
    val OkMsg                       = "Ok"
    val TitleRequiredMsg            = "The 'title' field is required"
    val TitleEmptyMsg               = "Title cannot be empty"
    val InvalidIdMsg                = "ID must be a positive number"
    val CreateFailedMsg             = "Failed to create task"
    val CreatedMsg                  = "Task successfully created"

    def UpdatedMsg(id: Int)         = s"Task $id has been updated"
    def DeletedMsg(id: Int)         = s"Task $id has been deleted"
    def CompletedMsg(id: Int)       = s"Task $id has been completed"
    def UncompletedMsg(id: Int)     = s"Task $id has been marked as not completed"
    def NotFoundMsg(id: Int)        = s"Task $id not found"

    def CompletedAllMsg(n: Int)     = s"$n task(s) completed"
    def UncompletedAllMsg(n: Int)   = s"$n task(s) marked as not completed"
    def DeletedCompletedMsg(n: Int) = s"$n completed task(s) deleted"

    def AddIncorrectDataErrorMsg    = "Invalid data for creating task: "
    def UpdateIncorrectDataErrorMsg = "Invalid data for updating task: "
    def GetAllErrorMsg              = "Error retrieving task list"
    def GetActiveErrorMsg           = "Error retrieving active tasks"
    def GetCompletedErrorMsg        = "Error retrieving completed tasks"
    def GetByIdErrorMsg(id: Int)    = s"Error retrieving task with id=$id"
    val CreatedErrorMsg             = "Error creating task"
    def UpdatedErrorMsg(id: Int)    = s"Error updating task with id=$id"
    def DeletedErrorMsg(id: Int)    = s"Error deleting task with id=$id"
    def CompletedErrorMsg(id: Int)  = s"Error completing task with id=$id"
    def UncompletedErrorMsg(id: Int)= s"Error marking task with id=$id as not completed"
    def CompletedAllErrorMsg        = "Error completing all tasks"
    def UncompletedAllErrorMsg      = "Error uncompleting all tasks"
    def DeletedCompletedErrorMsg    = "Error deleting completed tasks"
  }
}
