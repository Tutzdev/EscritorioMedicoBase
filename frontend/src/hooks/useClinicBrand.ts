import { useQuery } from "@tanstack/react-query";
import { apiRequest } from "../api/client";
import type { PublicClinicSettings } from "../types/api";

export function useClinicBrand() {
  return useQuery({
    queryKey: ["clinic-brand"],
    queryFn: () =>
      apiRequest<PublicClinicSettings>("/clinic-settings/public", {
        skipAuthentication: true,
      }),
    staleTime: 5 * 60_000,
  });
}
