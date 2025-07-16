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
Text[6]=["Magnakai Disciplines","Magnakai Disciplines are special abilities that you can use at certain points in each book. As you gain certain combinations of Disciplines you will also gain Lore-Circles, which give a bonus to your CS and/or EP. Choose carefully."]
Text[7]=["Bonus Disciplines","You may select one (1) Bonus Discipline for each Magnakai adventure you have previously completed. So the more books you finish the stronger you become!"]
Text[8]=["Weapons","You may carry up to two (2) weapons at any given time.<br /><br />If you are holding a Weapon and have the appropriate 'Weaponmastery' discipline for that weapon you gain a +3(CS) bonus. If you enter combat without any weapons you take a -4(CS) penalty."]
Text[9]=["Belt Pouch","You are limited to a maximum of fifty (50) Gold Crowns"]
Text[10]=["Meals","Each Meal you carry counts as one (1) backpack item. If you don't have a meal when you're instructed to eat you must take a -3(EP) penalty."]
Text[11]=["Backpack Items","You use your backpack to carry all of the common items you pick up on your adventure. You are limited to a maximum of eight (8) items. Each meal you carry also counts as one (1) item..."]
Text[12]=["Special Items","Many items you find along the way will be designated as 'Special Items' and are not carried in your backpack. You are limited to a maximum of twelve (12) 'Special Items'. Since some of these items can be very useful and others may be worthless, be selective!..."]
Text[13]=["Game Notes","Use this area to keep notes on game play and to keep track of any Special Items you may have in safe keeping..."]
Text[14]=["Save & Load","Select one of the three (3) save game slots and click the appropriate button to either load or save your current game data."]
Text[15]=["Data Backup","Click the 'Backup Data' button to load all of your current action chart data into the text box below. You can then copy the data and save it in a plain text file on your computer. If your saved game ever gets erased for any reason you can then paste the data back into the text box, press the 'Restore Data' button and load it back into the action chart."]
Text[16]=["Magnakai Lore-Circles","Lore Circles grant a bonus to your CS and/or EP (This bonus is permanent and should be added to your Base score). You gain a Lore-Circle when you have learned each of the required Magnakai Disciplines for that specific Lore-Circle. If you finish all of the Magnakai adventures you will eventualy gain all four Lore-Circles."]
Text[17]=["Circle of Fire","You must have Weaponmastery and Huntmastery to gain this Lore-Circle"]
Text[18]=["Circle of Light","You must have Animal Control and Curing to gain this Lore-Circle"]
Text[19]=["Circle of Solaris","You must have Invisiblity, Huntmastery and Pathmanship to gain this Lore-Circle"]
Text[20]=["Circle of the Spirit","You must have Psi-surge, Psi-shield, Nexus and Divination to gain this Lore-Circle"]
Text[21]=["Weaponmastery Checklist","When you learn the Weaponmastery discipline you may choose three (3) weapons from the list below. You may also choose one (1) weapon for each Magnakai adventure you complete later. Therefor, it is best to choose this discipline early on. <br /> <br /> If you enter combat holding a weapon that you have selcted you gain a +3(CS) bonus. If you select the Bow, add three (3) to any Random Number you pick while using the Bow."]
Text[22]=["Quiver","Your quiver is used to carry arrows. <br><br> The Game Rules do not specify the maximum number of arrows that your quiver can carry. Just be logical. I would personally suggest a maximum limit of twelve (12)."]
Text[23]=["Psi-Surge","YOU MAY ONLY USE THIS OPTION IF YOU HAVE LEARNED THE PSI-SURGE DISCIPLINE. <br /><br /> With this option selected the combat system will add four (4) points to your Combat Skill, and you will suffer two (2) points of damage for each round of combat."]
Text[24]=["Mind Force","Use the following check boxes if you wish to use either of the two Mind Force disciplines durring this combat."]
Text[25]=["Mindblast","YOU MAY ONLY USE THIS OPTION IF YOU HAVE LEARNED THE PSI-SURGE DISCIPLINE. <br /><br /> This weaker version of the Mind Force will add two (2) points to your Combat Skill and you will not suffer any additional damage."]

Style[0]=["white","black","#006331","#FFFFE6","","","","","","","","","2","2",300,"",2,2,10,10,"","","","",""]
Style[1]=["white","black","#006331","#FFFFE6","","","","","","","","sticky","2","2",300,"",2,2,10,10,"","","","",""]

applyCssFilter()

