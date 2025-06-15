---
title: Capstone Portfolio
layout: default
---

---
# Professional Self Assessment

To be completed

---

# Artifact Selection

The capstone project for the Computer Science program at SNHU was required to demonstrate my skills in the three key categories: Software Design and Engineering, Algorithms and Data Structure, and Databases. 
I chose to select one artifact that I could work with to show my skills in all three categories. I chose to work with the weight tracking Android application that I origninaly developed in CS 360: Mobile architecture in Programming that I took in Fall 2024. 
This was a very basic weight tracking application developed in Android Studio. It allowed users to log in, track their weight, set a goal weight, and edit and delete entries using a local SQLite database. 

The enhancements of my weight tracking application were done in stages, first tackling Software Design and Engineering, then Algorithms and Data Structure, and then Databases. Below you'll find narratives for these enhancements as they were developed.

---

# Code Review

Below is a link to a video of a code review of the original application as it was at the beginning of the class with planned enhancements. You can see a walkthrough of the app early in the video and it's clear how basic the functionality of the application is.

<iframe width="560" height="315"
        src="https://www.youtube.com/embed/giIBgWjw55k"
        frameborder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen>
</iframe>

---

# Software Design and Engineering

Text goes here.

---

# Algorithms and Data Structures

My application at the end of CS 360 had limited functionality for the user, so I felt there were opportunities to enhance the application in the area of Algorithms and Data Structure to provide a more meaningful experience to the user. At the start of this capstone course, the application simply displayed a list of weight entries to the user; they couldn’t draw any meaningful conclusions from the data other than receiving a notification when they reached their goal.

To enhance the user experience, I added custom max and min heap classes to track the highest and lowest weights efficiently. I also created a WeightTrendAnalyzer class that uses logic to detect the longest streak of consecutive days a user logs their weight, the longest streak of days their weight decreased, and their longest weight plateau where they saw very little change. So now instead of only seeing a list of entries, the user sees a visual graph of their weight progress as well as some quick stats regarding their min and max weight, as well as some streak data. There is evidence that streaks encourage users to engage more with apps (Anizoba, 2025), so including this as an enhancement could encourage the user to meet their weight goals and use the app more often.

Below you can see the progress of the development of the application. The first image is at the end of CS 360, very basic, just a list of entries with dates. The second image is at the end of the Software Design and Engineering Milestone; overall, a better, more professional looking app with some meaningful information with the addition of a chart. Then the third image is the app currently, I’ve formatted the chart better with more meaningful date labels on the x-axis, it’s zoomable and scrollable, and has a clear goal marker on it. There are also some quick and meaningful stats for the user right below the chart and then the scrollable list is below that. I also formatted the dates to be in the format that most Americans are used to (month/date/year).

<h4>Project Screenshots</h4>
<div style="display: flex; justify-content: center; gap: 20px; flex-wrap: wrap;">

  <div style="flex: 1; max-width: 250px; text-align: center;">
    <img src="images/CS%20360%20Homepage.png" alt="CS 360 Homepage" style="width: 100%; height: auto; border: 1px solid #ccc; border-radius: 8px;">
    <p>CS 360 Homepage</p>
  </div>

  <div style="flex: 1; max-width: 250px; text-align: center;">
    <img src="images/Software%20Design%20%26%20Engineering%20Milestone.png" alt="Software Design & Engineering" style="width: 100%; height: auto; border: 1px solid #ccc; border-radius: 8px;">
    <p>Design & Engineering Milestone</p>
  </div>

  <div style="flex: 1; max-width: 250px; text-align: center;">
    <img src="images/Algorithms%20%26%20Data%20Structures%20Milestone.png" alt="Algorithms & Data Structures" style="width: 100%; height: auto; border: 1px solid #ccc; border-radius: 8px;">
    <p>Algorithms & Data Structures Milestone</p>
  </div>

</div>

## Reflection
Enhancing and modifying this artifact  for the Algorithms and Data Structures portion of the capstone project taught me a lot about how to take a simple app and start adding more meaningful features behind the scenes. Figuring out how to calculate trends like streaks and plateaus pushed me to think more carefully about how to structure the data and how to compare dates and weights efficiently. 

One challenge I ran into was formatting and displaying the results clearly, especially when dealing with date ranges and keeping the UI consistent. Another was making sure I didn’t break anything while adding logic that handled edge cases, like entries with the same weight or missing days. 

I also realized that I have a habit of writing a lot of logic directly in HomeActivity, even when it would probably be cleaner and easier to manage in its own class. I’m definitely still learning how to better organize my code. Working through these improvements helped me get better at writing code that’s not just functional, but also more maintainable and user-friendly.

---

# Databases

Text goes here.
