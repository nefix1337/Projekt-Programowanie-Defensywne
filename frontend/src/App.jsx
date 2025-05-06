import { AuthProvider } from "./auth/AuthProvider";
import Routes from "./routes/index.jsx";

function App() {
  return (
    <AuthProvider>
      <Routes />
    </AuthProvider>
  );
}

export default App;
