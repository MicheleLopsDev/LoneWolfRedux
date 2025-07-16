/*
Please refer to readme.html for full Instructions

Text[...]=[title,text]

Style[...]=[TitleColor,TextColor,TitleBgColor,TextBgColor,TitleBgImag,TextBgImag,TitleTextAlign,TextTextAlign, TitleFontFace, TextFontFace, TipPosition, StickyStyle, TitleFontSize, TextFontSize, Width, Height, BorderSize, PadTextArea, CoordinateX , CoordinateY, TransitionNumber, TransitionDuration, TransparencyLevel ,ShadowType, ShadowColor]
*/

var FiltersEnabled = 0 // if your not going to use transitions or filters in any of the tips set this to 0

Text[0]=["Read Me","Lone Wolf Action Chart 3.6.9! <br /><br /> The collapsed & expanded feature does not work in the download version. My appologies... <br /><br /> Placing your mouse over the Information icons will display tool-tips with instructions and game rules for each section. <br /><br /><A href='javascript:testBrowser()'>Click Here</a> to check how much data is being stored on your machine."]
Text[1]=["Combat System","Enter your Enemy's Combat Skill and Endurance Points below, and select any of the combat options you wish to use. Then click the 'Start Combat' button to use the Automated Combat System."]
Text[2]=["Fight to the Death","With this option selected the combat system will calculate rounds of combat, back to back, until you or your enemy are dead! Then display the final results."]
Text[3]=["Random Number Generator","At various points in each book you will be instructed to 'Pick a number from the Random Number Table'. At these points simply click the 'Random Number' button to get a number between 0-9"]
Text[4]=["Combat Skill","Your Combat Skill represents your battle prowess and greatly effects the outcome of combat. It is calculated by taking your Base Score and adding any bonuses you may have from skills and items to get your Total score."]
Text[5]=["Endurance Points","Your Endurance Points represent your life. If they ever fall to zero (0) or below, you are dead... They are calculated by taking your Base Score and adding any bonuses you may have from skills and items to get your Total score.<br /><br />The Status Bar and your 'Current EP' will update automatically when you use the Combat System."]
Text[6]=["Grand Master Disciplines","Grand Master Disciplines are special skills that you can use at certain points in each book. Using the correct skill at the right time may mean the difference between life and death... Choose carefully."]
Text[7]=["Bonus Disciplines","You gain one (1) additional Discipline, and a bonus of +1(CS) and +2(EP) (This bonus is permanent and should be added to your Base score) for each Grand Master adventure you have previously completed. Each Discipline also becomes stronger as you advance through the Ranks as a Grand Master. So the more adventures you finish the stronger you become."]
Text[8]=["Weapons","You may carry up to two (2) weapons. If you are holding a Weapon and have the appropriate Grand Weaponmastery for that weapon you gain a +5(CS) bonus."]
Text[9]=["Grand Weaponmastery","When you learn the skill of Grand Weaponmastery you may check two (2) weapons from the list below. You may also check one (1) weapon for each previous Grand Master adventure you have completed. <br /> <br /> If you enter combat holding a weapon that you have selcted you gain a +5(CS) bonus. If you select the Bow, add three (3) to any Random Number you pick while using the Bow."]
Text[10]=["Belt Pouch","You are limited to a maximum of fifty (50) Gold Crowns"]
Text[11]=["Meals","Each Meal you carry counts as one (1) backpack item. If you don't have a meal when you're instructed to eat you must take a -3(EP) penalty."]
Text[12]=["Quiver","Your quiver is used to carry arrows. <br><br> The Game Rules do not specify the maximum number of arrows that your quiver can carry. Just be logical. I would personally suggest a maximum limit of twelve (12)."]
Text[13]=["Backpack Items","You use your backpack to carry all of the common items you pick up on your adventure. You are limited to a maximum of ten (10) items. Each meal you carry also counts as one (1) item..."]
Text[14]=["Special Items","Many items you find along the way will be designated as 'Special Items' and are not carried in your backpack. You are limited to a maximum of twelve (12) 'Special Items'. Since some of these items can be very useful and others may be worthless, be selective!..."]
Text[15]=["Game Notes","Use this area to keep notes on game play and to keep track of any Special Items you may have in safe keeping..."]
Text[16]=["Save & Load","Select one of the three (3) save game slots and click the appropriate button to either load or save your current game data."]
Text[17]=["Data Backup","Click the 'Backup Data' button to load all of your current action chart data into the text box below. You can then copy the data and save it in a plain text file on your computer. If your saved game ever gets erased for any reason you can then paste the data back into the text box, press the 'Restore Data' button and load it back into the action chart."]

Style[0]=["white","black","#006331","#FFFFE6","","","","","","","","","2","2",300,"",2,2,10,10,"","","","",""]
Style[1]=["white","black","#006331","#FFFFE6","","","","","","","","sticky","2","2",300,"",2,2,10,10,"","","","",""]

applyCssFilter()

