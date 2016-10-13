require 'watir'
require 'Login_IE'
require 'Parameters'

browser = Watir::IE.start($server_url)
#browser.minimize

login = Login.new(browser)
login.login()

browser.link(:text, "Reports").click
#browser.maximize
