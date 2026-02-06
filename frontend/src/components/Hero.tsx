type HeroProps = {
  subtitle: string;
  title?: string;
  tags?: string[];
  onGenerateOrder?: () => void;
  generateLabel?: string;
};

const Hero = ({ subtitle, title = "", tags = [], onGenerateOrder, generateLabel }: HeroProps) => {
  return (
    <section className="hero">
      <div>
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </div>
      <div className="hero-actions">
        {onGenerateOrder && generateLabel ? (
          <button className="button ghost" onClick={onGenerateOrder}>
            {generateLabel}
          </button>
        ) : null}
        {tags.length > 0 && (
          <div className="pill-group">
            {tags.map((tag) => (
              <span key={tag} className="pill">
                {tag}
              </span>
            ))}
          </div>
        )}
      </div>
    </section>
  );
};

export default Hero;
