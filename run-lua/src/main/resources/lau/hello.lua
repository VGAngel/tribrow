function getRoomDescription()
    return "Конференция"
end

function processMessage(adapter)
    local message = adapter:getMessage()
    local user = adapter:getUser()
    local nick = user:getNick()

    adapter:sendMessageToRoomAndUser(user, nick..": "..message, "")
end

function userEnter(adapter)
    local user = adapter:getUser()
    local nick = user:getNick()
    adapter:sendMessageToRoomAndUser(user, "Вошел пользователь "..nick, "Вы вошли в конференцию")
end

function userLeft(adapter)
    local nick = adapter:getUser():getNick()
    adapter:sendMessageToRoom("Пользоатель "..nick.." вышел из конференции")
end