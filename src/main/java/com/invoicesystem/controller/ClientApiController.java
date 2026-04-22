package com.invoicesystem.controller;

import com.invoicesystem.dto.ClientDto;
import com.invoicesystem.model.Client;
import com.invoicesystem.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientApiController {

    @Autowired
    private ClientService clientService;

    @GetMapping
    public List<Client> getAllClients() {
        return clientService.getAllClients();
    }

    @PostMapping
    public Client createClient(@RequestBody ClientDto.Request dto) {
        return clientService.createClient(dto);
    }
}
