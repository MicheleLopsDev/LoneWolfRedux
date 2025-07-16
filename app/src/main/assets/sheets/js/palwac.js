// -------------------------------------------------------
// - Lone Wolf Action Chart & Combat System 3.6.9
// - eric.zollman@projectaon.org
// - http://www.projectaon.org/staff/eric
// -------------------------------------------------------
// - This script utilizes the Prototype 1.7 Framework &
// - the jStorage wrapper plugin. For more info visit:
// - www.prototypejs.org & www.jstorage.info
// -------------------------------------------------------

// Declare all 'global' variables & load values if needed.
var cr;
var cs;
var ecs;
var cep;
var ecep;
var dmg;
var edmg;
var tdmg;
var kaiName;
var death;
var edeath;
var rounds;
var fttd;
var rn;
var lastNum;
var chartID;
var chartState = true;
var mainFrame = parent.document.getElementById("mainFrame");

 // addOneEP() - add one (1) to the current EP text box and call the findPercentage() function.
function addOneEP() {
    var score;
    score = $('actionChart').epNow.value;
    score++;
    $('actionChart').epNow.value = score;
    findPercentage();
 }

 // chromeCheck() - check to see if the page is loaded in the Chrome browser.
 // if so use a special CSS file.
 function chromeCheck() {
    var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;

      if (is_chrome) {
        $('cssPlace').innerHTML = '<link type="text/css" href="css/chrome.css" rel="stylesheet" />';
      }
  }

 // testBrowser() - test the browser for javascript & html5 support
 function testBrowser() {
    var value;
    var value1;
    var value2;

    value = $.jStorage.storageAvailable();
    if (!value) {
      alert("Your browser does not support Local Storage", "Error!");
    } else {
      value1 = $.jStorage.currentBackend();
      value2 = $.jStorage.storageSize();
      alert("Your browser is JavaScript & HTML5 compatible. \n \n Your current jStorage Backend is: " + value1 + "\n This site is currently storing " + value2 + " bytes of data.", "Success!");
  }
 }

 // addEP() - add base and bonus score for EP
 function addEP() {
   var base;
   var bonus;
   var total;

   base = $('actionChart').epBase.value;
   bonus = $('actionChart').epBonus.value;
   base = (+base);
   bonus = (+bonus);
   total = (base + bonus);
   $('actionChart').epTotal.value = total;

  }

// addCS() - add base and bonus score for CS
 function addCS() {
   var base;
   var bonus;
   var total;

   base = $('actionChart').csBase.value;
   bonus = $('actionChart').csBonus.value;
   base = (+base);
   bonus = (+bonus);
   total = (base + bonus);
   $('actionChart').csTotal.value = total;

  }

// getRanNum() - get a Random Number between 0 and 'limit'
 function getRanNum(limit) {
     var newNum;

     newNum = Math.floor(Math.random()*limit);

 return newNum;
 }

// randomNumber() - display six random numbers one after another, stopping on the last one.
 function randomNumber(place, limit) {
    var node = $(place);

    if (!node) {
      return;
    }

    if (lastNum === null) {
      lastNum = 0;
    }

  ranNum = getRanNum(limit);

    if (ranNum==lastNum){
      ranNum = getRanNum(limit);
    }

      setTimeout(function(){
                node.innerHTML = " &nbsp;&nbsp;&nbsp;<b>.</b>";
                }, 150);
      setTimeout(function(){
                node.innerHTML = " &nbsp;&nbsp;&nbsp;<b>..</b>";
                }, 300);
     setTimeout(function(){
                node.innerHTML = " &nbsp;&nbsp;&nbsp;<b>...</b>";
                }, 450);
     setTimeout(function(){
                node.innerHTML = " &nbsp;&nbsp;&nbsp;<b>....</b>";
                }, 600);
     setTimeout(function(){
                node.innerHTML = " &nbsp;&nbsp;&nbsp;<b>" + ranNum + "</b>";
                }, 750);

    lastNum = ranNum;
}

// saveAllData() - loop through all of the action chart elements and save each one using
// jStorage to handle the localStorage method.
 function saveAllData() {
    var fieldType;
    var fieldName;
    var fieldValue;
    var saveName;
    var userSave;
    
    $('actionChart').leftURL.value = window.top.frames['One'].location.href;

    userSave = $('optionForm').saveGame.value;
      if (userSave == "Save 01") {
      userSave = "";
    }

    chartID = $('combatForm').chartID.value;

   for(i=0; i < document['actionChart'].elements.length; i++){

    fieldType = document['actionChart'].elements[i].type;
    fieldName = document['actionChart'].elements[i].name;

    if(fieldType == 'checkbox') {
      fieldValue = document['actionChart'].elements[i].checked;
    } else {
      fieldValue = document['actionChart'].elements[i].value;
    }

    if (fieldValue !== null) {
      saveName = chartID + fieldName + userSave;
      $.jStorage.set(saveName, fieldValue);

    }
  }

}

// loadAllData() - loop through all the action chart elements to check for saved data and load
// the data for each element if it exists.
 function loadAllData() {
    var fieldType;
    var fieldName;
    var fieldValue;
    var saveName;
    var userSave;

    userSave = $('optionForm').saveGame.value;
     if (userSave == "Save 01") {
      userSave = "";
    }


    chartID = $('combatForm').chartID.value;

  for(i=0; i < document['actionChart'].elements.length; i++){

    fieldType = document['actionChart'].elements[i].type;
    fieldName = document['actionChart'].elements[i].name;
    saveName = chartID + fieldName + userSave;
    fieldValue = $.jStorage.get(saveName);


      if (fieldValue !== null) {

        if(fieldType == 'checkbox') {

             if (fieldValue === true) {
                document['actionChart'].elements[i].checked = true;
                   } else {
                document['actionChart'].elements[i].checked = false;
             }

        } else {
          document['actionChart'].elements[i].value = fieldValue;
        }

      }
  }


     var URL = $('actionChart').leftURL.value;

       if (URL !== null){
         
         if (URL == "undefined"){ URL = ""; }
         
        parent.One.location = URL;
        }


  findPercentage();

 }

 // findPercentage() - get the total EP and current EP values, check for empty or non-numerical
 // data, convert from string to number, check that current EP is not greater than the total EP,
 // calculate their percenrage and update the progress bar.
  function findPercentage(){
      var epNow;
      var epTotal;
      var sendBack;

      epNow = $('actionChart').epNow.value;
      epTotal = $('actionChart').epTotal.value;

      if ( !epTotal || isNaN(epTotal) || epTotal===0 ) {
          return;
      }

      epNow = (+epNow);
      epTotal = (+epTotal);

        if (epNow > epTotal){
          epNow = epTotal;
          $('actionChart').epNow.value = epNow;
        }
      sendBack = (epNow / epTotal) * 100;
      sendBack = Math.round(sendBack);
      myJsProgressBarHandler.setPercentage('element1', sendBack);

      //return sendBack;
  }




 // getInfo() - get the user input, check it for errors & empty data, reset variables and call
 // a function to either 'fight one round' or 'fight to the death'.
 function getInfo(){

    fttd = $('combatForm').fttd.checked;
    cs = $('actionChart').csTotal.value;
    ecs = $('combatForm').enemyCS.value;
    cep = $('actionChart').epNow.value;
    ecep = $('combatForm').enemyEP.value;
    kaiName = $('actionChart').kaiName.value;
    death  = false;
    edeath = false;
    rounds  = 0;
    tdmg = 0;


  // Make sure the info is not blank & it's a valid entry
    if (kaiName.length<=2 || kaiName.length>=14) {
        alert("NOTICE:\n You must select a Kai Name!\n Between 3 and 13 letters long.\n");
        return;
    }

        if ( !cep || isNaN(cep) ) {
          alert("NOTICE:\n You have entered a non-numerical value for: Current EP.\n");
          return;
    }
      if ( !cs || isNaN(cs) ) {
          alert("NOTICE:\n You have entered a non-numerical value for: Total CS.\n");
          return;
    }
        if ( !ecep || isNaN(ecep) ) {
          alert("NOTICE:\n You have entered a non-numerical value for: Enemy EP.\n");
          return;
    }
        if ( !ecs || isNaN(ecs) ) {
          alert("NOTICE:\n You have entered a non-numerical value for: Enemy CS.\n");
          return;
    }

 // if info is good calc the Combat Ratio
 cr = (cs - ecs);

 



  // if "Fight to the Death" is selected, execute the fightToTheDeath()
  // function otherwise execute the fightOneRound() function.
    if (fttd){
  fightToTheDeath();
  } else {
    fightOneRound();
    }

}

 // fightOneRound() - determine the results of one combat round, update all of our variables, and
 // call a display function depending on the state of death.
 function fightOneRound(){
  rn = getRanNum(10);

 // Calculate damage and load variables for CR 0
    if (cr===0) {
        switch(rn) {
        case 1: edmg=3; dmg=5; break;
        case 2: edmg=4; dmg=4; break;
        case 3: edmg=5; dmg=4; break;
        case 4: edmg=6; dmg=3; break;
        case 5: edmg=7; dmg=2; break;
        case 6: edmg=8; dmg=2; break;
        case 7: edmg=9; dmg=1; break;
        case 8: edmg=11; dmg=0; break;
        case 9: edmg=12; dmg=0; break;
        case 0: edmg=12; dmg=0; break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR 1 & 2

    if (cr==1 || cr==2) {
        switch(rn) {
        case 1: edmg=4; dmg=5; break;
        case 2: edmg=5; dmg=4; break;
        case 3: edmg=6; dmg=3; break;
        case 4: edmg=7; dmg=3; break;
        case 5: edmg=8; dmg=2; break;
        case 6: edmg=9; dmg=2; break;
        case 7: edmg=10; dmg=1; break;
        case 8: edmg=11; dmg=0; break;
        case 9: edmg=12; dmg=0; break;
        case 0: edmg=14; dmg=0; break;
        default: break;
        }
    }


    // Calculate damage and load variables for CR 3 & 4

    if (cr==3 || cr==4) {
        switch(rn) {
        case 1: edmg=5; dmg=4; break;
        case 2: edmg=6; dmg=3; break;
        case 3: edmg=7; dmg=3; break;
        case 4: edmg=8; dmg=2; break;
        case 5: edmg=9; dmg=2; break;
        case 6: edmg=10; dmg=2; break;
        case 7: edmg=11; dmg=1; break;
        case 8: edmg=12; dmg=0; break;
        case 9: edmg=14; dmg=0; break;
        case 0: edmg=16; dmg=0; break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR 5 & 6

    if (cr==5 || cr==6) {
        switch(rn) {
        case 1: edmg=6; dmg=4; break;
        case 2: edmg=7; dmg=3; break;
        case 3: edmg=8; dmg=3; break;
        case 4: edmg=9; dmg=2; break;
        case 5: edmg=10; dmg=2; break;
        case 6: edmg=11; dmg=1; break;
        case 7: edmg=12; dmg=0; break;
        case 8: edmg=14; dmg=0; break;
        case 9: edmg=16; dmg=0; break;
        case 0: edmg=18; dmg=0; break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR 7 & 8

    if (cr==7 || cr==8) {
        switch(rn) {
        case 1: edmg=7; dmg=4; break;
        case 2: edmg=8; dmg=3; break;
        case 3: edmg=9; dmg=2; break;
        case 4: edmg=10; dmg=2; break;
        case 5: edmg=11; dmg=2; break;
        case 6: edmg=12; dmg=1; break;
        case 7: edmg=14; dmg=0; break;
        case 8: edmg=16; dmg=0; break;
        case 9: edmg=18; dmg=0; break;
        case 0: edmg=999; dmg=0; break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR 9 & 10

    if (cr==9 || cr==10) {
        switch(rn) {
        case 1: edmg=8; dmg=3; break;
        case 2: edmg=9; dmg=3; break;
        case 3: edmg=10; dmg=2; break;
        case 4: edmg=11; dmg=2; break;
        case 5: edmg=12; dmg=2; break;
        case 6: edmg=14; dmg=1; break;
        case 7: edmg=16; dmg=0; break;
        case 8: edmg=18; dmg=0; break;
        case 9: edmg=999; dmg=0; break;
        case 0: edmg=999; dmg=0;  break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR 11+

    if (cr >= 11) {
        switch(rn) {
        case 1: edmg=9; dmg=3; break;
        case 2: edmg=10; dmg=2; break;
        case 3: edmg=11; dmg=2; break;
        case 4: edmg=12; dmg=2; break;
        case 5: edmg=14; dmg=1; break;
        case 6: edmg=16; dmg=1; break;
        case 7: edmg=18; dmg=0;  break;
        case 8: edmg=999; dmg=0; break;
        case 9: edmg=999; dmg=0; break;
        case 0: edmg=999; dmg=0; break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR -1 & -2

    if (cr == -1 || cr == -2) {
        switch(rn) {
        case 1: edmg=2; dmg=5; break;
        case 2: edmg=3; dmg=5; break;
        case 3: edmg=4; dmg=4; break;
        case 4: edmg=5; dmg=4; break;
        case 5: edmg=6; dmg=3; break;
        case 6: edmg=7; dmg=2; break;
        case 7: edmg=8; dmg=2; break;
        case 8: edmg=9; dmg=1; break;
        case 9: edmg=10; dmg=0; break;
        case 0: edmg=11; dmg=0; break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR -3 & -4

    if (cr == -3 || cr == -4) {
        switch(rn) {
        case 1: edmg=1; dmg=6; break;
        case 2: edmg=2; dmg=5; break;
        case 3: edmg=3; dmg=5; break;
        case 4: edmg=4; dmg=4; break;
        case 5: edmg=5; dmg=4; break;
        case 6: edmg=6; dmg=3; break;
        case 7: edmg=7; dmg=2; break;
        case 8: edmg=8; dmg=1; break;
        case 9: edmg=9; dmg=0; break;
        case 0: edmg=10; dmg=0; break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR -5 & -6

    if (cr == -5 || cr == -6) {
        switch(rn) {
        case 1: edmg=0; dmg=6; break;
        case 2: edmg=1; dmg=6; break;
        case 3: edmg=2; dmg=5; break;
        case 4: edmg=3; dmg=5; break;
        case 5: edmg=4; dmg=4; break;
        case 6: edmg=5; dmg=4; break;
        case 7: edmg=7; dmg=2; break;
        case 8: edmg=8; dmg=0; break;
        case 9: edmg=8; dmg=0; break;
        case 0: edmg=9; dmg=0; break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR -7 & -8

    if (cr == -7 || cr == -8) {
        switch(rn) {
        case 1: edmg=0; dmg=8; break;
        case 2: edmg=0; dmg=7; break;
        case 3: edmg=1; dmg=6; break;
        case 4: edmg=2; dmg=6; break;
        case 5: edmg=3; dmg=5; break;
        case 6: edmg=4; dmg=5; break;
        case 7: edmg=5; dmg=4; break;
        case 8: edmg=6; dmg=3; break;
        case 9: edmg=7; dmg=2; break;
        case 0: edmg=8; dmg=0; break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR -9 & -10

    if (cr == -9 || cr == -10) {
        switch(rn) {
        case 1: edmg=0; dmg=999; break;
        case 2: edmg=0; dmg=8; break;
        case 3: edmg=0; dmg=7; break;
        case 4: edmg=1; dmg=7; break;
        case 5: edmg=2; dmg=6; break;
        case 6: edmg=3; dmg=6; break;
        case 7: edmg=4; dmg=5; break;
        case 8: edmg=5; dmg=4; break;
        case 9: edmg=6; dmg=3; break;
        case 0: edmg=7; dmg=0; break;
        default: break;
        }
    }

    // Calculate damage and load variables for CR -11 or lower

    if (cr <= -11) {
        switch(rn) {
        case 1: edmg=0; dmg=999; break;
        case 2: edmg=0; dmg=999; break;
        case 3: edmg=0; dmg=8; break;
        case 4: edmg=0; dmg=8; break;
        case 5: edmg=1; dmg=7; break;
        case 6: edmg=2; dmg=6; break;
        case 7: edmg=3; dmg=5; break;
        case 8: edmg=4; dmg=4; break;
        case 9: edmg=5; dmg=3; break;
        case 0: edmg=6; dmg=0; break;
        default: break;
        }
    }

// increment round, subtract damage from you & enemy, add total damage.
    rounds++;
    ecep-=edmg;
    cep-=dmg;
    tdmg+=dmg;

  // Check if you or your enemy are dead
   if (cep < 1){
        death=true;
    }

    if (ecep < 1){
        edeath=true;
    }

  // If somebody is dead, display death results, If nobody is dead but the
  // "Fight to the Death" option is selected return false & display nothing.
  // If nobody is dead and option is not selected show live results.
    if (death || edeath) {
        deathResults();
    } else if (fttd) {
        return;
    } else {
        liveResults();
    }
   return;
 }

 // deathResults() - display results & update the action chart if somebody is dead.
 function deathResults(){

    if (death) {
        alert("YOUR LIFE AND YOUR QUEST END HERE!! \n \nYour enemy delt you a Fatal Blow \nafter " + rounds + " Round(s) of Combat... \n \n" + kaiName + " took " + tdmg + " total damage - \nand now has 0 Endurance Points. \n");
        $('actionChart').epNow.value = "0";
    findPercentage();
        return;
    }

    if (edeath) {
        alert("YOU HAVE DEFEATED YOUR ENEMY!! \n \nYour Combat Ratio is: " + cr + ". \nThe combat lasted " + rounds + " round(s). \n \n" + kaiName + " took " + tdmg + " total damage - \nand now has "+ cep +" Endurance Points. \n");
    $('actionChart').epNow.value = cep;
    findPercentage();
        return;
    }
}

// liveResults() - display results, update the action chart & ask to fight another round.
 function liveResults(){

    var agn = window.confirm("Combat Round: " + rounds + "\nYour Combat Ratio is: " + cr + "\nYour Random Number is: "+ rn + "\n \nYour Enemy took "+ edmg +" damage - \nand now has "+ ecep +" Endurance Points. \n \n"+ kaiName +" took "+ dmg +" damage - \nand now has "+ cep +" Endurance Points. \n \nWould you like to fight another round? \n");

    if (agn) {
        fightOneRound();
    } else {
        alert("You decided to stop fighting after " + rounds + "\nRound(s) of Combat. You may now \n'Evade Combat'. (if available) \n \n" + kaiName + " took " + tdmg + " total damage - \nand now has "+ cep +" Endurance Points. \n");
    $('actionChart').epNow.value = cep;
    findPercentage();
       return;
    }

}

// fightToTheDeath() - loop through the fightOneRound() function until somebody is dead.
 function fightToTheDeath(){
 var i;
 i = 1;

  while (i!==2){

    if (death){
    break;
  } else if (edeath) {
    break;
  } else {
      fightOneRound();
    }

  }

}

// getBackup() fetches all of the data from the action chart and loads it into a text area 
// in a formated way
function getBackup() {
    var fieldType;    
    var fieldValue;
    var allData;
    
	$('backup').backupData.value = "";
	allData = $('backup').backupData.value

    for(i=0; i < document['actionChart'].elements.length; i++){

    fieldType = document['actionChart'].elements[i].type;
 
    if(fieldType == 'checkbox') {
      fieldValue = document['actionChart'].elements[i].checked;
    } else {
      fieldValue = document['actionChart'].elements[i].value;
    }

    if (fieldValue !== null) {
      allData = $('backup').backupData.value;
		
      $('backup').backupData.value = allData + "{" + fieldValue + "}";

    }
  }

}

  // loadBackup() reads the data from the text-area then parses the data and 
  // loads it back into the action chart
  function loadBackup() {
    var x;
    var y = 0;
    var z;
    var i = 0;
    var fieldType;
    var backupData = $('backup').backupData.value;
 

  while (i < document['actionChart'].elements.length) {
	x = backupData.indexOf("{", y);        
	y = backupData.indexOf("}", x);       
	z = backupData.substring(x+1, y);

        fieldType = document['actionChart'].elements[i].type;
        
             if (z !== null) {

        if(fieldType == 'checkbox') {

             if (z === "true") {
                document['actionChart'].elements[i].checked = true;
                   } else {
                document['actionChart'].elements[i].checked = false;
             }

        } else {
          document['actionChart'].elements[i].value = z;
        }

      }
        
         i++
         
    }
        $('backup').backupData.value = "";
        
       findPercentage();
        
    var URL = $('actionChart').leftURL.value;

       if (URL !== null){
        parent.One.location = URL;
        }
    
    }
	
	// changeFramePos() - check the value of the chartState variable then
	// collapse or expand the right frame accordingly and change the value
	// of chartState to reflect the change. if the value of "reset" is passed
	// into the function then return the frameset to it's original centered state.
	function changeFramePos(resetFrame) {
		
		if (chartState){
			chartState = false;
            mainFrame.setAttribute("cols", "*,50px");
		} else {
		    chartState = true;
            mainFrame.setAttribute("cols", "5%,*");
		}
		
		if (resetFrame == "reset"){
			chartState = true;
			mainFrame.setAttribute("cols", "50%,*");
		}
	}
	

// EOF

/* Version History

3.6.9 Jan 2013 - Added a 'Backup' function (by user request) that allows the user to export and import 
 data which can be saved on the users machine in a txt file, to help prevent a 'total loss' should the 
 LocalStorage data ever get deleted. Added three floating icons. One to colapse or expand the Action
 Chart (by user request). Making the chart easier to use on small screens, such as an iPad or tablet. 
 A second icon to restore the frameset to it's default position, and the third as a "quick save", 
 allowing the user to save data in slot 1 while the chart is collapsed. Added combat options so the
 combat script will work correctly when using Mindblast, Psi-Surge, ect... Added a single exe distro
 for windows users and a basic ZIP distro for Linux and Mac users (once again, by request). 
 Incorperated a new tool-tip script that uses CSS formating and looks much better 
 (thanks to Essam Gamal)...

3.2 June 2012 - Re-wrote the entire script to use LocalStorage (HTML5) and to use the 
 Prototype Framework. Combat system now updates the action chart durring combat and has a
 'fight to the death' option (by user request). Added a progress bar to display current EP level 
 (by user request). The Action chart now does some basic math for you (by user request), allows you
 to save up to three (3) seperate games (by user request) and when loading a game the left frame is
 redirected to the numbered section you last saved on (also by user request).

2.1.1 Feb 2011 - Added a sef-extracting distro for the action chart., Changed the section
 number area and added graphics to show browser support., Reformated tool-tips to make them
 more uniform... Wish list: Save games without using cookies. They are unreliable at best,
 some people disable cookies, they are clunky to work with and I don't want the end user to
 lose their data unexpectedly.

2.1 May 2010 - Made some changes to the combat code. Random number is now writen directly to
 the page instead of using an alert box (by user request) improving user interaction.
 Optimized code! Reducing file size from 16 kb to 11 kb, and reducing the codes line count
 from 1564 to 368! While also incorperating a check for enabled cookies, allowing for combat
 ratio's to be entered above and below the -11 to 11 limit, checking user input for errors,
 and fixing my old junky error hanlding! Also fixed a small error to do with entering blank
 data and returning erroneous output... Also changed Random number function to produce a
 MORE random number and prevent the same number from being selected back to back more than
 should be expected.

2.0 Apr 2010 - Made the official move to the project aon server! Replaced the three prompt
 boxes with text fields, (by user request) making the user interaction much smoother.
 Updated grafics and changed the file structure to accomadate the project aon site
 structure. Made a change to combat code in order to catch an error when a combat ratio over
 11 or under -11 was entered. Also added extra text boxes to the Endurance Points and Combat
 Skill section to now keep track of your Base number, bonuses, and current score...

1.6 Feb 2010 - Re-worked entire combat code! Combat now supports sequential rounds of combat,
 continuing until Lone Wolf or the Enemy is dead, or until the user stops combat in order to
 evade. More combat output, including combat round, combat ratio, random number, damage to
 both sides and amount of ep remaining for both sides. Updated Action chart for use with ALL
 of the lone wolf books, expanding coverage from books 1-5 to books 1-28.

1.5 Jan 2010 - Dressed up action chart a lot! Added css formating and grafics to make page
 conform with style of the main project aon site. Added JavaScript mouseOver tool-tips to
 provide indepth instructions and clarification for each section. Added DIV boxes to seperate
 sections and make chart look cleaner and more stream-line. Small adjustments to combat code.

1.2 Oct 2009 - Cleaned up action chart, adding some directions and text for clarification.

1.1 Oct 2009 - Added basic combat system & random number generator. One round of combat only.
 Prompt box's to input endurance points, combat skill, and enemy ep. Random number presented
 in alert box. Damage for lone wolf & enemy presented with alert box.

1.0 Sep 2009 - First implimentation. VERY basic chart for use with books 1-5 only...
 Coded for my own use. No combat system, just a few text fields that could be
 saved and loaded using cookies.

*/

