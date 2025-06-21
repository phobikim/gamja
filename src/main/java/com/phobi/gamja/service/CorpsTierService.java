package com.phobi.gamja.service;

import com.phobi.gamja.repository.user.UserCorpsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CorpsTierService {
    private final UserCorpsRepository userCorpsRepository;
    
}
