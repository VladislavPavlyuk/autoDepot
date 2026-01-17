type HeroProps = {
  subtitle: string;
  tags: string[];
};

const Hero = ({ subtitle, tags }: HeroProps) => {
  return (
    <section className="hero">
      <div>
        <h1>Today overview</h1>
        <p>{subtitle}</p>
      </div>
      <div className="pill-group">
        {tags.map((tag) => (
          <span key={tag} className="pill">
            {tag}
          </span>
        ))}
      </div>
    </section>
  );
};

export default Hero;
