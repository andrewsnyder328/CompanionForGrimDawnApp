# CompanionForGrimDawnApp
Android app for planning devotion builds in Grim Dawn

This app was built in my free time for fun as I was playing Grim Dawn and wanted a tool to help plan devotion paths.

After getting it to a working state that I was satisfied with, I ceased development. 
There are still many improvements that could be made, and I would be happy to welcome contributions.

If you have something you'd like to see added, changed, or an issue fixed, feel free to submit a pull request and I will address it as soon as I can.

### Data schema for devotions:
```
{
    "name": String, //Name of the devotion
    "points": Integer, //How many individual points it contains
    "requirements": { //Object of how many points of each type you need to unlock
      "ascendant": Integer //Field for each type (ascendant, chaos, eldritch, order, primordial)
    },
    "rewards": { //Object of how much of each type of point you will gain by fully completing this devotion
      "ascendant": Integer
    },
    "imageSrc": String, //Name of matching devotion drawable resource from `app/src/main/res/drawable/`
    "description": String //Description for the devotion
  }
  ```
  
  To run the app, clone this repo and run in Android Studio.
