package br.com.sample.model

import com.fasterxml.jackson.annotation.JsonProperty

data class NotificationRequest(var title: String?, var content: String?, var push: Push?)

data class Push(@JsonProperty("include_user_ids") var users: List<Long>?)

data class NotificationResponse(var uuid: String)

data class NotificationProcessResponse(var usersWithError: List<Long>)