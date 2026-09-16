import { CheckCircle2, CircleAlert } from "lucide-react";

export function FeedbackMessage({
  type,
  children,
}: {
  type: "success" | "error";
  children: string;
}) {
  return (
    <div className={`feedback feedback--${type}`} role={type === "error" ? "alert" : "status"}>
      {type === "success" ? (
        <CheckCircle2 size={17} aria-hidden="true" />
      ) : (
        <CircleAlert size={17} aria-hidden="true" />
      )}
      <span>{children}</span>
    </div>
  );
}
