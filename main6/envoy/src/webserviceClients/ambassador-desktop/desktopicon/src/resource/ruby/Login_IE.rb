require 'watir'
require 'Parameters'

class Login
	def browser
		@browser
	end

	def initialize(p_browser)
		@browser = p_browser
	end
	
	def login()
		#@browser.goto($server_url)
		if @browser.button(:value, "Login").exists?
			@browser.text_field(:name, "nameField").set($username)
			@browser.text_field(:name, "passwordField").set($password)
			@browser.button(:name, "login0").click
		end
	end
end

#browser = Watir::IE.new()

#login = Login.new(browser)
#login.login()

#browser = FireWatir::Firefox.new()

#login = Login.new(browser)
#login.login()