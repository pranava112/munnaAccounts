package com.vpm.Accounts.Controller;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@CrossOrigin(origins= {"*"})
public class MainController {
	
	@GetMapping
	public String getResponse(){
		return "Welcome to Accounting Dashboard";
	}

}
