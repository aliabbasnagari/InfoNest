# **InfoNest**

A Cloud-Backed Notes App using Firebase

## Objective
Develop a Notes App with full CRUD operations, using Firebase Authentication and Cloud Firestore to store and sync notes in real time.

## Core Features
User Sign Up / Login (Firebase Authentication)

Add, Edit, Delete Notes (Firestore)

Realtime syncing across devices (Firestore live updates)

Notes stored per user (only visible to them)

Responsive, clean UI using Material Design

## App Structure
LoginActivity – Firebase Auth login/signup

MainActivity – Shows notes list

NoteEditorActivity – Create/edit a note

Note – data model

NoteAdapter – RecyclerView adapter

FirebaseRepository – handles all Firebase logic

NoteViewModel – business logic and LiveData management


# Requirements
1. Firebase Setup
   Connect app to Firebase
   Enable Email/Password Authentication
   Setup Cloud Firestore with secure user-specific rules:
   jsonCopyEditrules_version = '2';
   service cloud.firestore {  match /databases/{database}/documents {    match /notes/{noteId} {      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
   }} }
2. Authentication
   LoginActivity with:
   Email/password fields
   Sign up and login buttons
   Redirect to MainActivity on success
3. Notes List
   MainActivity:
   RecyclerView showing all notes for logged-in user
   FAB to add new note
   Realtime updates using Firestore snapshot listener
   Option to log out
4. Note Editor
   NoteEditorActivity:
   Title and content fields
   Save and delete buttons
   If editing an existing note, populate data
   Save/Update to Firestore
5. ViewModel + Repository
   NoteViewModel: Exposes LiveData for notes list
   FirebaseRepository: Handles Firebase Auth + Firestore
   Bonus Features (Optional)
   Mark note as favorite
   Add search/filter by title or content
   Add labels/tags per note
   Add dark mode
   Upload images to Firebase Storage and display in notes
   Testing Requirements
   Validate empty fields
   Handle Firebase errors (e.g. invalid login)
   Test offline caching (Firestore supports offline mode)
   Learning Goals
   Firebase integration: Auth, Firestore, Realtime data
   Full MVVM architecture
   RecyclerView optimization
   Firebase Security Rules
   Offline-first data persistence
   Clean UI with Material Components

## License

[MIT](https://choosealicense.com/licenses/mit/)