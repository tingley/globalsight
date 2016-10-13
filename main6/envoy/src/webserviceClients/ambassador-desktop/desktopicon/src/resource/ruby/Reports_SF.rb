require 'rubygems'
require 'safariwatir'

browser = Watir::Safari.new
browser.goto("")
#browser.minimize

if browser.button(:value, "Login").exists?
browser.text_field(:name, "nameField").set("")
browser.text_field(:name, "passwordField").set("")
browser.button(:name, "login0").click
end

browser.link(:text, "Reports").click
#ff.maximize
