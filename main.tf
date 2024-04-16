terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }
  required_version = ">= 1.2.0"
}

provider "aws" {
  region = "us-west-2"
}

data "aws_iam_policy_document" "lambda_policy_doc" {
  statement {
    actions   = ["sts:AssumeRole"]
    effect    = "Allow"
    principals {
      identifiers = ["lambda.amazonaws.com"]
      type        = "Service"
    }
  }
}

resource "aws_iam_role" "lambda_execution_role" {
  name               = "lambda_execution_role"
  assume_role_policy = data.aws_iam_policy_document.lambda_policy_doc.json
}

variable "lambda_functions" {
  type = map(object({
    handler = string
    path    = string
  }))
  default = {
    author = {
      handler = "app.net.lambda.AuthorSearchHandler::handleRequest",
      path    = "author"
    },
    title = {
      handler = "app.net.lambda.TitleSearchHandler::handleRequest",
      path    = "title"
    },
    topic = {
      handler = "app.net.lambda.TopicSearchHandler::handleRequest",
      path    = "topic"
    }
  }
}

resource "aws_lambda_function" "search_lambda" {
  for_each = var.lambda_functions

  function_name = "Lambda${title(each.key)}Search"
  handler       = each.value.handler
  role          = aws_iam_role.lambda_execution_role.arn
  runtime       = "java17"

  filename         = "/Users/jonahkunzler/CS204/204-final-project-JonahKunzler/target/module-14.jar"
  source_code_hash = filebase64sha256("/Users/jonahkunzler/CS204/204-final-project-JonahKunzler/target/module-14.jar")
}

resource "aws_api_gateway_rest_api" "api_gateway" {
  name        = "SearchAPI"
  description = "API Gateway for search functions"
}

resource "aws_api_gateway_resource" "api_resource" {
  for_each    = var.lambda_functions
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  parent_id   = aws_api_gateway_rest_api.api_gateway.root_resource_id
  path_part   = each.value.path
}

resource "aws_api_gateway_method" "api_method" {
  for_each      = var.lambda_functions
  rest_api_id   = aws_api_gateway_rest_api.api_gateway.id
  resource_id   = aws_api_gateway_resource.api_resource[each.key].id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "api_integration" {
  for_each              = var.lambda_functions
  rest_api_id           = aws_api_gateway_rest_api.api_gateway.id
  resource_id           = aws_api_gateway_resource.api_resource[each.key].id
  http_method           = aws_api_gateway_method.api_method[each.key].http_method
  type                  = "AWS_PROXY"
  integration_http_method = "POST"
  uri                   = aws_lambda_function.search_lambda[each.key].invoke_arn
}

resource "aws_api_gateway_deployment" "api_deployment" {
  depends_on = [
    aws_api_gateway_integration.api_integration
  ]
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  stage_name  = "v1"
}

resource "aws_lambda_permission" "api_lambda_permission" {
  for_each = var.lambda_functions

  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.search_lambda[each.key].function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.api_gateway.execution_arn}/*/${aws_api_gateway_method.api_method[each.key].http_method}${aws_api_gateway_resource.api_resource[each.key].path}"
}
