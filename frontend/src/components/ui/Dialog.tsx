import * as RadixDialog from "@radix-ui/react-dialog";
import { X } from "lucide-react";
import type { ReactNode } from "react";

interface DialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  children: ReactNode;
  footer?: ReactNode;
  width?: "md" | "lg";
}

export function Dialog({
  open,
  onOpenChange,
  title,
  description,
  children,
  footer,
  width = "md",
}: DialogProps) {
  return (
    <RadixDialog.Root open={open} onOpenChange={onOpenChange}>
      <RadixDialog.Portal>
        <RadixDialog.Overlay className="dialog-overlay" />
        <RadixDialog.Content className={`dialog dialog--${width}`}>
          <header className="dialog__header">
            <div>
              <RadixDialog.Title className="dialog__title">
                {title}
              </RadixDialog.Title>
              {description ? (
                <RadixDialog.Description className="dialog__description">
                  {description}
                </RadixDialog.Description>
              ) : null}
            </div>
            <RadixDialog.Close className="icon-button" aria-label="Fechar">
              <X size={18} />
            </RadixDialog.Close>
          </header>
          <div className="dialog__content">{children}</div>
          {footer ? <footer className="dialog__footer">{footer}</footer> : null}
        </RadixDialog.Content>
      </RadixDialog.Portal>
    </RadixDialog.Root>
  );
}
