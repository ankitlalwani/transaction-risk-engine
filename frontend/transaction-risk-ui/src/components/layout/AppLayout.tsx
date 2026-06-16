import { Outlet } from "react-router-dom";
import { Sidebar } from "./Sidebar";
import { Header } from "./Header";

export function AppLayout() {
  return (
    <div className="app-shell">
      <Sidebar />

      <main className="main-content">
        <Header />
        <section className="page-content">
          <Outlet />
        </section>
      </main>
    </div>
  );
}