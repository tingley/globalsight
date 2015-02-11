require 'firewatir'

browser = FireWatir::Firefox.start("")
#browser.minimize

if browser.button(:value, "Login").exists?
browser.text_field(:name, "nameField").set("")
browser.text_field(:name, "passwordField").set("")
browser.button(:name, "login0").click
end

browser.link(:text, "My Jobs").click
browser.select_list(:name, "sto").select("Pending")
browser.button(:value, "Search").click
#browser.maximize
