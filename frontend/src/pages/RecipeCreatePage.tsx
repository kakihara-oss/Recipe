import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import type { IngredientMaster } from '../api/client';
import api from '../api/client';

interface StepForm {
  description: string;
  durationMinutes: string;
  temperature: string;
  tips: string;
}

interface IngredientForm {
  ingredientId: string;
  quantity: string;
  unit: string;
  preparationNote: string;
}

export default function RecipeCreatePage() {
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [ingredients, setIngredients] = useState<IngredientMaster[]>([]);

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('');
  const [servings, setServings] = useState('2');
  const [concept, setConcept] = useState('');
  const [story, setStory] = useState('');

  const [steps, setSteps] = useState<StepForm[]>([
    { description: '', durationMinutes: '', temperature: '', tips: '' },
  ]);

  const [recipeIngredients, setRecipeIngredients] = useState<IngredientForm[]>([
    { ingredientId: '', quantity: '', unit: '', preparationNote: '' },
  ]);

  useEffect(() => {
    api.get<{ content: IngredientMaster[] }>('/ingredients', { params: { size: 100 } })
      .then((res) => setIngredients(res.data.content))
      .catch(() => {});
  }, []);

  const addStep = () => {
    setSteps([...steps, { description: '', durationMinutes: '', temperature: '', tips: '' }]);
  };

  const removeStep = (index: number) => {
    setSteps(steps.filter((_, i) => i !== index));
  };

  const updateStep = (index: number, field: keyof StepForm, value: string) => {
    const updated = [...steps];
    updated[index] = { ...updated[index], [field]: value };
    setSteps(updated);
  };

  const addIngredient = () => {
    setRecipeIngredients([...recipeIngredients, { ingredientId: '', quantity: '', unit: '', preparationNote: '' }]);
  };

  const removeIngredient = (index: number) => {
    setRecipeIngredients(recipeIngredients.filter((_, i) => i !== index));
  };

  const updateIngredient = (index: number, field: keyof IngredientForm, value: string) => {
    const updated = [...recipeIngredients];
    updated[index] = { ...updated[index], [field]: value };
    setRecipeIngredients(updated);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      const payload = {
        title,
        description,
        category,
        servings: parseInt(servings) || 2,
        concept,
        story,
        cookingSteps: steps
          .filter((s) => s.description.trim())
          .map((s, i) => ({
            stepNumber: i + 1,
            description: s.description,
            durationMinutes: s.durationMinutes ? parseInt(s.durationMinutes) : undefined,
            temperature: s.temperature || undefined,
            tips: s.tips || undefined,
          })),
        ingredients: recipeIngredients
          .filter((ri) => ri.ingredientId)
          .map((ri) => ({
            ingredientId: parseInt(ri.ingredientId),
            quantity: parseFloat(ri.quantity) || 0,
            unit: ri.unit,
            preparationNote: ri.preparationNote || undefined,
          })),
      };

      const res = await api.post('/recipes', payload);
      navigate(`/recipes/${res.data.id}`);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || 'レシピの作成に失敗しました');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <Link to="/" className="back-link">← 一覧に戻る</Link>
      <h2>新規レシピ作成</h2>

      {error && <div className="error-banner">{error}</div>}

      <form onSubmit={handleSubmit} className="recipe-form">
        <section className="form-section">
          <h3>基本情報</h3>
          <div className="form-group">
            <label>タイトル *</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              maxLength={200}
              placeholder="例: 特製カルボナーラ"
            />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>カテゴリ</label>
              <input
                type="text"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                placeholder="例: パスタ、前菜、デザート"
              />
            </div>
            <div className="form-group">
              <label>人数</label>
              <input
                type="number"
                value={servings}
                onChange={(e) => setServings(e.target.value)}
                min="1"
              />
            </div>
          </div>
          <div className="form-group">
            <label>説明</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              maxLength={2000}
              placeholder="レシピの概要"
            />
          </div>
          <div className="form-group">
            <label>コンセプト</label>
            <textarea
              value={concept}
              onChange={(e) => setConcept(e.target.value)}
              rows={2}
              placeholder="このレシピで表現したいこと"
            />
          </div>
          <div className="form-group">
            <label>ストーリー</label>
            <textarea
              value={story}
              onChange={(e) => setStory(e.target.value)}
              rows={2}
              placeholder="このレシピの背景にある物語"
            />
          </div>
        </section>

        {ingredients.length > 0 && (
          <section className="form-section">
            <div className="section-header">
              <h3>食材</h3>
              <button type="button" onClick={addIngredient} className="btn btn-secondary btn-small">
                + 食材を追加
              </button>
            </div>
            {recipeIngredients.map((ri, index) => (
              <div key={index} className="dynamic-row">
                <select
                  value={ri.ingredientId}
                  onChange={(e) => updateIngredient(index, 'ingredientId', e.target.value)}
                >
                  <option value="">食材を選択</option>
                  {ingredients.map((ing) => (
                    <option key={ing.id} value={ing.id}>
                      {ing.name}（{ing.standardUnit}）
                    </option>
                  ))}
                </select>
                <input
                  type="number"
                  value={ri.quantity}
                  onChange={(e) => updateIngredient(index, 'quantity', e.target.value)}
                  placeholder="数量"
                  step="0.1"
                />
                <input
                  type="text"
                  value={ri.unit}
                  onChange={(e) => updateIngredient(index, 'unit', e.target.value)}
                  placeholder="単位"
                />
                <input
                  type="text"
                  value={ri.preparationNote}
                  onChange={(e) => updateIngredient(index, 'preparationNote', e.target.value)}
                  placeholder="下処理メモ"
                />
                {recipeIngredients.length > 1 && (
                  <button type="button" onClick={() => removeIngredient(index)} className="btn-remove">
                    ×
                  </button>
                )}
              </div>
            ))}
          </section>
        )}

        <section className="form-section">
          <div className="section-header">
            <h3>調理手順</h3>
            <button type="button" onClick={addStep} className="btn btn-secondary btn-small">
              + 手順を追加
            </button>
          </div>
          {steps.map((step, index) => (
            <div key={index} className="step-form-item">
              <div className="step-form-number">{index + 1}</div>
              <div className="step-form-fields">
                <div className="form-group">
                  <input
                    type="text"
                    value={step.description}
                    onChange={(e) => updateStep(index, 'description', e.target.value)}
                    placeholder="手順の説明 *"
                  />
                </div>
                <div className="form-row-compact">
                  <input
                    type="number"
                    value={step.durationMinutes}
                    onChange={(e) => updateStep(index, 'durationMinutes', e.target.value)}
                    placeholder="時間（分）"
                  />
                  <input
                    type="text"
                    value={step.temperature}
                    onChange={(e) => updateStep(index, 'temperature', e.target.value)}
                    placeholder="温度"
                  />
                  <input
                    type="text"
                    value={step.tips}
                    onChange={(e) => updateStep(index, 'tips', e.target.value)}
                    placeholder="コツ・ポイント"
                  />
                </div>
              </div>
              {steps.length > 1 && (
                <button type="button" onClick={() => removeStep(index)} className="btn-remove">
                  ×
                </button>
              )}
            </div>
          ))}
        </section>

        <div className="form-actions">
          <button type="submit" disabled={submitting} className="btn btn-primary btn-large">
            {submitting ? '作成中...' : 'レシピを作成'}
          </button>
          <Link to="/" className="btn btn-secondary btn-large">キャンセル</Link>
        </div>
      </form>
    </div>
  );
}
