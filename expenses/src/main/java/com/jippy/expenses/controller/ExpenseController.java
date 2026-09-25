package com.jippy.expenses.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/expenses")
public class ExpenseController {

    @GetMapping("/get")
    public String expenses(){
        return "expenses are called";
    }
}
