function l10n(key) {return key;}

var selectQuestion = l10n('select a question');
// must match (lowercased) text that is first item of question list in HTML select

var months = [l10n("january"),l10n("february"),l10n("march"),l10n("april"),l10n("may"),l10n("june"),l10n("july"),l10n("august"),l10n("september"),l10n("october"),l10n("november"),l10n("december")];
// must match (lowercased) text that is HTML select for b-day

var problemText = l10n("There is a problem with your information. Please double check what you have entered.\n\nError ");
// used for an alert, if there some unforseen problem

var checkMarkAltTxt = l10n("Congrats! The content that you have entered into this field is sufficient.");
var xMarkAltTxt = l10n("Error! The content that you have entered into this field is not sufficient.");
var whoops = l10n('Whoops...');
var problemInfo = l10n('It looks like there was an problem with some of the information that you entered.');
var fixProblem = l10n('Please take a look below and correct any fields marked with an error.');
var abc_from_welocalize = abc('Please take a look below and correct \\\\any fields marked with an error from abc.');



var err = new Array();

err['{actionForm.zipCode}'] = new Array();
err['{actionForm.zipCode}'][2] = l10n("Please make sure the Postal Code you entered is 5 \\digits long.");
err['{actionForm.zipCode}'][3] = err['{actionForm.zipCode}'][2];
err['{actionForm.zipCode}'][4] = err['{actionForm.zipCode}'][2];
err['{actionForm.zipCode}'][5] = l10n("Your Postal Code may not contain characters such as @, !, *, $, or letters.  Please re-enter your Postal Code using only numbers now.");
err['{actionForm.zipCode}'][7] = err['{actionForm.zipCode}'][2];

err['{actionForm.alternateEmailAddress}'] = new Array();
err['{actionForm.alternateEmailAddress}'][1] = l10n("The Email Address you entered, is not formatted correctly.\nPlease re-enter your Email Address now using this format: name@somewhere.com.");

err['{actionForm.password}'] = new Array();
err['{actionForm.password}'][1] = l10n("Please enter a Password that is 6-16 characters using only letters and numbers.");
err['{actionForm.password}'][2] = l10n("Your Password may not contain characters such as @, !, * or $.\nPlease enter a Password using only letters and numbers.");
err['{actionForm.password}'][3] = err['{actionForm.password}'][1];
err['{actionForm.password}'][4] = err['{actionForm.password}'][1];
err['{actionForm.password}'][5] = err['{actionForm.password}'][1];
err['{actionForm.password}'][7] = err['{actionForm.password}'][1];
err['{actionForm.password}'][102] = l10n("The Passwords you entered do not match. Please re-enter your Password exactly the same in both fields.");
err['{actionForm.password}'][103] = l10n("Your Screen Name and Password are too similar.  Please enter a new Password now.");
err['{actionForm.password}'][104] = err['{actionForm.password}'][103];

err['{actionForm.reTypePassword}'] = new Array();
err['{actionForm.reTypePassword}'][1] = err['{actionForm.password}'][1];
err['{actionForm.reTypePassword}'][3] = err['{actionForm.password}'][1];
err['{actionForm.reTypePassword}'][4] = err['{actionForm.password}'][1];
err['{actionForm.reTypePassword}'][5] = err['{actionForm.password}'][1];
err['{actionForm.reTypePassword}'][7] = err['{actionForm.password}'][1];
err['{actionForm.reTypePassword}'][102] = err['{actionForm.password}'][102];

err['{actionForm.answer}'] = new Array();
err['{actionForm.answer}'][1] = l10n("Please enter an Answer that is 3-80 characters.");
err['{actionForm.answer}'][2] = l10n("Your Answer may not contain characters such as @, !, * or $.\nPlease enter a Answer using only letters and numbers.");
err['{actionForm.answer}'][103] = l10n("Please select an Account Security Question.");

err['{actionForm.reTypeAnswer}'] = new Array();
err['{actionForm.reTypeAnswer}'][1] = err['{actionForm.answer}'][1];
err['{actionForm.reTypeAnswer}'][102] = l10n("The Answers that you entered do not match. Please re-enter your Answer exactly the same in both fields.");

err['{actionForm.year}'] = new Array();
err['{actionForm.year}'][1] = l10n("Please enter the four digit year that you were born.");
err['{actionForm.year}'][2] = err['{actionForm.year}'][1];
err['{actionForm.year}'][3] = l10n("You must be over 13 years of age to register for this service.");
err['{actionForm.year}'][4] = l10n("Please select the day that you were born.");
err['{actionForm.year}'][5] = l10n("Please select the month that you were born.");

err['wlw-select_key:{actionForm.day}'] = new Array();
err['wlw-select_key:{actionForm.day}'][1] = l10n("Please select the day that you were born.");
err['wlw-select_key:{actionForm.day}'][3] = err['{actionForm.year}'][3]

err['wlw-select_key:{actionForm.month}'] = new Array();
err['wlw-select_key:{actionForm.month}'][1] = l10n("Please select the month that you were born.");
err['wlw-select_key:{actionForm.month}'][3] = err['{actionForm.year}'][3]

err['wlw-radio_button_group_key:{actionForm.gender}'] = new Array();
err['wlw-radio_button_group_key:{actionForm.gender}'][1] = l10n("Please select your gender.");

err['{actionForm.imageCharacters}'] = new Array();
err['{actionForm.imageCharacters}'][1] = l10n("Please enter the characters in the image above without any spaces");


