package com.thinkmart.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thinkmart.dto.ContactRequest;
import com.thinkmart.model.ContactMessage;
import com.thinkmart.repository.ContactMessageRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;

    public ContactController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> send(@Valid @RequestBody ContactRequest request) {
        ContactMessage m = new ContactMessage();
        m.setName(request.name().trim());
        m.setEmail(request.email().trim());
        m.setSubject(request.subject());
        m.setMessage(request.message().trim());
        contactMessageRepository.save(m);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Thanks! We have received your message."));
    }
}
