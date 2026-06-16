type BadgeVariant = "default" | "success" | "warning" | "danger" | "neutral";

type BadgeProps = {
  children: React.ReactNode;
  variant?: BadgeVariant;
};

export function Badge({ children, variant = "default" }: BadgeProps) {
  return <span className={`badge badge-${variant}`}>{children}</span>;
}