package com.ChatApp;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeTest;


@SpringBootTest
class DemoApplicationTests {
	
	WebDriver driver;
	String signUpurl="https://mypack.bcoder.co.in/signup";
	String firstName="Vishal", lastName="Kumar",email="xyz@yopmail.com";
	String password="Qwerty@123" , referralCode;
	
	@BeforeTest
	public void openUrl()
	{
		this.driver=new ChromeDriver();
		this.driver.get(signUpurl);
		driver.manage().window().maximize();

	}
	
	@Test
	public void fillForm()
	{
		driver.findElement(By.xpath("//*[@id=\"firstName\"]")).click();
		driver.findElement(By.xpath("//*[@id=\"firstName\"]")).sendKeys(firstName);
		driver.findElement(By.xpath("//*[@id=\"lastName\"]")).click();
		driver.findElement(By.xpath("//*[@id=\"lastName\"]")).sendKeys(lastName);
		driver.findElement(By.xpath("//*[@id=\"email\"]")).click();
		driver.findElement(By.xpath("//*[@id=\"email\"]")).sendKeys(email);
		driver.findElement(By.xpath("//*[@id=\"password\"]")).click();
		driver.findElement(By.xpath("//*[@id=\"password\"]")).sendKeys(password);
		driver.findElement(By.xpath("//*[@id=\"confirmPassword\"]")).click();
		driver.findElement(By.xpath("//*[@id=\"confirmPassword\"]")).sendKeys(password);
		
		
		

		

		

	}
	

}
