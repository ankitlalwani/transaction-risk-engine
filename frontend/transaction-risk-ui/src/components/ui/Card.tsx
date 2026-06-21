type CardProps = {
  title?: string;
  variant?: "blue" | "green" | "amber" | "red" | "purple" | "slate";
  children: React.ReactNode;
};

export function Card({ title, variant, children }: CardProps) {
  return (
    <div className={`card${variant ? ` card-${variant}` : ""}`}>
      {title && <h2 className="card-title">{title}</h2>}
      {children}
    </div>
  );
}
