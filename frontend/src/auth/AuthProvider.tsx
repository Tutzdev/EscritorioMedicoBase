import { useQuery, useQueryClient } from "@tanstack/react-query";
import {
  type ReactNode,
  useEffect,
  useMemo,
} from "react";
import {
  apiRequest,
  clearTokens,
  hasStoredSession,
  loginRequest,
  storeTokens,
} from "../api/client";
import type { CurrentUser } from "../types/api";
import { AuthContext, type AuthContextValue } from "./AuthContext";

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const sessionQuery = useQuery({
    queryKey: ["current-user"],
    queryFn: () => apiRequest<CurrentUser>("/auth/me"),
    enabled: hasStoredSession(),
    retry: false,
  });

  useEffect(() => {
    const handleUnauthorized = () => {
      clearTokens();
      queryClient.setQueryData(["current-user"], null);
    };

    window.addEventListener("medical-office:unauthorized", handleUnauthorized);
    return () => {
      window.removeEventListener(
        "medical-office:unauthorized",
        handleUnauthorized,
      );
    };
  }, [queryClient]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user: sessionQuery.data ?? null,
      isLoading: sessionQuery.isLoading,
      login: async (email, password) => {
        const response = await loginRequest(email, password);
        storeTokens(response.tokens);
        queryClient.setQueryData(["current-user"], response.user);
      },
      logout: () => {
        clearTokens();
        queryClient.clear();
      },
    }),
    [queryClient, sessionQuery.data, sessionQuery.isLoading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
