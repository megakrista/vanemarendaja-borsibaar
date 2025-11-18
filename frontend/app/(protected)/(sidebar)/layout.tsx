"use client";

import { ReactNode } from "react";
import { usePathname } from "next/navigation";
import {
  SidebarProvider,
  SidebarInset,
  SidebarTrigger,
} from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/sidebar";

// Protected layout wraps pages that require auth.
// Authentication and onboarding flow are handled by middleware.
export default function ProtectedLayout({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  // Hide sidebar completely on POS ID pages (e.g., /pos/123)
  const isPOSIDPage = pathname?.match(/^\/pos\/[^/]+$/);

  return (
    <SidebarProvider>
      {!isPOSIDPage && <AppSidebar />}
      <SidebarInset>
        {!isPOSIDPage && (
          <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4 lg:hidden">
            <SidebarTrigger className="-ml-1" />
          </header>
        )}
        {children}
      </SidebarInset>
    </SidebarProvider>
  );
}
