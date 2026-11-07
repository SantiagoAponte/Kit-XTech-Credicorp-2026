package com.andes.example.application.port.in;

import com.andes.example.application.dto.CreatePingCommand;
import com.andes.example.application.dto.CreatePingResult;

public interface CreatePing {
    CreatePingResult execute(CreatePingCommand command);
}
