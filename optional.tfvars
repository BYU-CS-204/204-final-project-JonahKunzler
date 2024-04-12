lambda = {
  partOne = {handler = "app.net.lambda.AuthorSearchHandler::handleRequest"}
  partTwo = {handler = "app.net.lambda.TitleSearchHandler::handleRequest"}
  partThree = {handler = "app.net.lambda.TopicSearchHandler::handleRequest"}
}

api_resource = {
  partOne = {pathPart = "author"}
  partTwo = {pathPart = "title"}
  partThree = {handler = "topic"}
}