import './App.css'
import React from "react";
import AppRoutes from "./AppRoutes.jsx";
import {AuthProvider, useAuth} from "./providers/AuthProvider.jsx";

function App() {

  return (

      <AuthProvider>
        <AppRoutes/>
      </AuthProvider>
  )
}

export default App
