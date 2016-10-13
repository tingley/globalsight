require 'watir'
require 'Login_IE'
require 'Parameters'

browser = Watir::IE.start($server_url)
#browser.minimize

login = Login.new(browser)
login.login()

browser.link(:text, "My Jobs").click
browser.select_list(:name, "sto").select("In Progress")
browser.button(:value, "Search").click
#browser.maximize
