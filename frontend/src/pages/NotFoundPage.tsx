import { ArrowLeft } from "lucide-react";
import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <main className="not-found">
      <p className="eyebrow">Página não encontrada</p>
      <h1>Este caminho não faz parte da rotina.</h1>
      <p>Volte para a visão do dia e continue de onde parou.</p>
      <Link className="button button--primary button--md" to="/">
        <ArrowLeft size={17} /> Voltar ao início
      </Link>
    </main>
  );
}
