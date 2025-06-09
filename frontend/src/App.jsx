import { AuthProvider } from "./auth/AuthProvider";
import Routes from "./routes/index.jsx";
import { Toaster } from 'sonner';

function App() {
  return (
    <AuthProvider>
      <Routes />
      <Toaster richColors position="top-right" />
    </AuthProvider>
  );
}

export default App;
